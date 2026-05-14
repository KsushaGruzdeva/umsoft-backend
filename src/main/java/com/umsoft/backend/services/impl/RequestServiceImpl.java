package com.umsoft.backend.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umsoft.backend.dtos.ClassificationResult;
import com.umsoft.backend.dtos.RequestDto;
import com.umsoft.backend.dtos.ResponseDto;
import com.umsoft.backend.entities.Category;
import com.umsoft.backend.entities.Department;
import com.umsoft.backend.entities.ProcessingResult;
import com.umsoft.backend.entities.Request;
import com.umsoft.backend.entities.Submitter;
import com.umsoft.backend.repositories.CategoryRepository;
import com.umsoft.backend.repositories.DepartmentRepository;
import com.umsoft.backend.repositories.ProcessingResultRepository;
import com.umsoft.backend.repositories.RequestRepository;
import com.umsoft.backend.repositories.SubmitterRepository;
import com.umsoft.backend.services.EmailService;
import com.umsoft.backend.services.GigaChatService;
import com.umsoft.backend.services.RequestService;

@Service
public class RequestServiceImpl implements RequestService {

    @Autowired
    private SubmitterRepository submitterRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProcessingResultRepository processingResultRepository;

    @Autowired
    private GigaChatService gigaChatService;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public ResponseDto processRequest(RequestDto requestDto) {
        // 1. Создаём отправителя
        Submitter submitter = new Submitter(
                requestDto.getFio(),
                requestDto.getEmail(),
                requestDto.getPhone()
        );
        submitterRepository.create(submitter);

        // 2. Создаём заявку со статусом NEW
        Request request = new Request(
                requestDto.getTask(),
                "NEW",
                submitter
        );

        Request savedRequest = requestRepository.create(request);
        System.out.println("✅ Заявка создана с ID: " + savedRequest.getId());

        // 3. Сразу отправляем подтверждение клиенту (асинхронно, с повторными попытками)
        sendImmediateConfirmation(savedRequest);

        // 4. Запускаем асинхронную обработку (классификация + письмо в отдел)
        processRequestAsync(savedRequest, requestDto.getTask());

        // 5. Немедленный ответ пользователю
        return new ResponseDto(
                true,
                "Заявка успешно принята в обработку! Если не пришло подтверждение на почту в течении трех минут, отправьте заново",
                savedRequest.getId(),
                savedRequest.getStatus()
        );
    }

    @Async
    public void sendImmediateConfirmation(Request request) {
        try {
            emailService.sendConfirmationToClient(request);
            System.out.println("📧 Письмо-подтверждение клиенту отправлено (асинхронно) для заявки ID: " + request.getId());
        } catch (Exception e) {
            System.err.println("❌ Ошибка при отправке письма-подтверждения клиенту для заявки ID: " + request.getId());
            e.printStackTrace();
        }
    }

    @Async
    @Transactional
    public void processRequestAsync(Request request, String taskDescription) {
        try {
            System.out.println("🔄 Начало асинхронной обработки заявки ID: " + request.getId());

            // Обновляем статус на PROCESSING
            request.setStatus("PROCESSING");
            requestRepository.update(request);

            // Отправляем запрос в GigaChat для классификации и суммаризации
            ClassificationResult classification = gigaChatService.classifyAndSummarize(taskDescription);

            System.out.println("🤖 Результат классификации: категория=" + classification.getCategoryName() +
                    ", уверенность=" + classification.getConfidenceScore());

            // Находим категорию и отдел по результатам классификации
            Category category = categoryRepository.findByName(classification.getCategoryName())
                    .orElse(null);

            Department department = null;
            if (category != null && category.getDepartment() != null) {
                department = category.getDepartment();
            } else {
                // Если категория не найдена, ищем отдел "Общий отдел"
                department = departmentRepository.findByName("Общий отдел").orElse(null);
                category = categoryRepository.findByName("Другое").orElse(null);
            }

            // Обновляем заявку результатами классификации
            request.setCategory(category);
            request.setAssignedDepartment(department);
            request.setStatus("CLASSIFIED");
            requestRepository.update(request);

            // Сохраняем результаты обработки
            ProcessingResult processingResult = new ProcessingResult(
                    classification.getSummary(),
                    request
            );
            processingResult.setConfidenceScore(classification.getConfidenceScore());
            processingResultRepository.create(processingResult);

            // Отправляем email в соответствующий отдел
            String assignedEmail = classification.getAssignedEmail();
            if (assignedEmail == null || assignedEmail.isEmpty()) {
                System.err.println("⚠️ Email отдела не получен от классификатора, использую email из отдела БД");
                if (department != null && department.getEmailAddress() != null) {
                    assignedEmail = department.getEmailAddress();
                } else {
                    assignedEmail = "info@umsoft.ru";
                }
            }

            System.out.println("📧 Отправка письма в отдел на email: " + assignedEmail);
            emailService.sendRequestEmail(
                    request,
                    classification.getCategoryName(),
                    classification.getSummary(),
                    assignedEmail
            );

            // Обновляем статус на SENT
            request.setStatus("SENT");
            requestRepository.update(request);

            System.out.println("✅ Заявка ID: " + request.getId() + " успешно обработана");

        } catch (Exception e) {
            System.err.println("❌ Ошибка при асинхронной обработке заявки ID: " + request.getId());
            e.printStackTrace();

            request.setStatus("ERROR");
            requestRepository.update(request);
        }
    }

    @Override
    public Request getRequestStatus(Long id) {
        return requestRepository.findById(id);
    }
}