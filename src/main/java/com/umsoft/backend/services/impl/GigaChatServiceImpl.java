package com.umsoft.backend.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umsoft.backend.dtos.ClassificationResult;
import com.umsoft.backend.entities.Category;
import com.umsoft.backend.entities.Department;
import com.umsoft.backend.repositories.CategoryRepository;
import com.umsoft.backend.repositories.DepartmentRepository;
import com.umsoft.backend.services.GigaChatService;

import chat.giga.client.GigaChatClient;
import chat.giga.model.completion.ChatMessage;
import chat.giga.model.completion.ChatMessageRole;
import chat.giga.model.completion.CompletionRequest;
import chat.giga.model.completion.CompletionResponse;

@Service
public class GigaChatServiceImpl implements GigaChatService {

    @Autowired
    private GigaChatClient gigaChatClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    // Кэш для категорий и отделов
    private List<Category> cachedCategories;
    private List<Department> cachedDepartments;
    private long lastCacheUpdate = 0;
    private static final long CACHE_TTL = 60000; // 60 секунд

    private List<Category> getCategories() {
        long now = System.currentTimeMillis();
        if (cachedCategories == null || (now - lastCacheUpdate) > CACHE_TTL) {
            cachedCategories = categoryRepository.findAll();
            lastCacheUpdate = now;
            System.out.println("Загружено категорий из БД: " + cachedCategories.size());
            cachedCategories.forEach(cat ->
                    System.out.println("  - " + cat.getName() + " (код: " + cat.getCode() +
                            ", отдел: " + (cat.getDepartment() != null ? cat.getDepartment().getName() : "не назначен") + ")")
            );
        }
        return cachedCategories;
    }

    private List<Department> getDepartments() {
        long now = System.currentTimeMillis();
        if (cachedDepartments == null || (now - lastCacheUpdate) > CACHE_TTL) {
            cachedDepartments = departmentRepository.findAll();
            System.out.println("Загружено отделов из БД: " + cachedDepartments.size());
        }
        return cachedDepartments;
    }

    private String buildCategoriesWithDepartmentsString() {
        List<Category> categories = getCategories();

        if (categories.isEmpty()) {
            return "нет доступных категорий";
        }

        // Формируем строку с категориями, их кодами и отделами
        return categories.stream()
                .map(cat -> {
                    String deptInfo = cat.getDepartment() != null ?
                            cat.getDepartment().getName() : "отдел не назначен";
                    return String.format("%s (код: %s, отдел: %s)", cat.getName(), cat.getCode(), deptInfo);
                })
                .collect(Collectors.joining("; "));
    }

    @Override
    public ClassificationResult classifyAndSummarize(String text) {
        try {
            List<Category> categories = getCategories();
            if (categories.isEmpty()) {
                return fallbackClassification(text);
            }
            String prompt = buildTechnicalPrompt(text);
            CompletionRequest request = CompletionRequest.builder()
                    .model("GigaChat-Pro")
                    .message(ChatMessage.builder()
                            .role(ChatMessageRole.SYSTEM)
                            .content("Ты - технический эксперт компании. " +
                                    "Преобразуй запрос в структурированное ТЗ. Верни строго JSON без пояснений.")
                            .build())
                    .message(ChatMessage.builder()
                            .role(ChatMessageRole.USER)
                            .content(prompt)
                            .build())
                    .temperature(0.2f)
                    .maxTokens(1000)
                    .build();
            CompletionResponse response = gigaChatClient.completions(request);
            if (response != null && response.choices() != null && !response.choices().isEmpty()) {
                String content = response.choices().get(0).message().content();
                return parseStructuredResponse(content, text);
            }
            return fallbackClassification(text);
        } catch (Exception e) {
            e.printStackTrace();
            return fallbackClassification(text);
        }
    }

    private String buildTechnicalPrompt(String text) {
        String categoriesInfo = buildCategoriesWithDepartmentsString();

        // Получаем список названий категорий и их кодов для JSON-схемы
        String categoriesList = getCategories().stream()
                .map(cat -> String.format("%s (код: %s)", cat.getName(), cat.getCode()))
                .collect(Collectors.joining(", "));

        return String.format("""
            Преобразуй следующий запрос клиента в структурированное техническое задание.
            
            Оригинальный запрос: "%s"
            
            Доступные категории: %s
            
            Ответ должен быть строго в формате JSON со следующей структурой:
            {
              "category_name": "точное название категории из списка выше",
              "category_code": "соответствующий код категории",
              "technical_task": {
                "title": "Краткое название задачи (до 10 слов)",
                "description": "Развернутое описание что нужно сделать",
                "requirements": ["Требование 1", "Требование 2", "Требование 3"],
                "deadline_estimate": "срочно/нормально/не срочно",
                "additional_notes": "дополнительные замечания"
              }
            }
            
            Правила:
            1. Не включай персональные данные
            2. Верни только JSON, без пояснений
            3. Категория должна быть выбрана строго из предложенного списка
            4. Укажи как название категории, так и её код
            """, text, categoriesList);
    }

    private ClassificationResult parseStructuredResponse(String response, String originalText) {
        try {
            String cleanResponse = response.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.startsWith("```")) {
                cleanResponse = cleanResponse.substring(3);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();

            JsonNode json = objectMapper.readTree(cleanResponse);

            // Пытаемся получить название и код категории
            String categoryName = json.has("category_name") ? json.get("category_name").asText() : null;
            String categoryCode = json.has("category_code") ? json.get("category_code").asText() : null;

            // Если нет category_name, пробуем старое поле category
            if (categoryName == null && json.has("category")) {
                categoryName = json.get("category").asText();
            }

            // Ищем категорию в БД (сначала по коду, потом по названию)
            Category matchedCategory = null;
            if (categoryCode != null) {
                matchedCategory = findCategoryByCode(categoryCode);
            }
            if (matchedCategory == null && categoryName != null) {
                matchedCategory = findCategoryByName(categoryName);
            }

            if (matchedCategory == null && categoryName != null) {
                // Пробуем найти похожую категорию
                matchedCategory = findSimilarCategory(categoryName);
                if (matchedCategory != null) {
                    categoryName = matchedCategory.getName();
                    categoryCode = matchedCategory.getCode();
                    System.out.println("Найдена похожая категория: " + categoryName + " (код: " + categoryCode + ")");
                }
            }

            String assignedEmail = null;
            String finalCategoryCode = null;
            String finalCategoryName = null;

            if (matchedCategory != null) {
                assignedEmail = findEmailByCategory(matchedCategory);
                finalCategoryCode = matchedCategory.getCode();
                finalCategoryName = matchedCategory.getName();
            } else {
                // Если категория не найдена, используем fallback
                System.err.println("Категория не найдена. Название: '" + categoryName + "', код: '" + categoryCode + "'");
                return fallbackClassification(originalText);
            }

            // Собираем структурированное ТЗ
            StringBuilder structuredSummary = new StringBuilder();
            JsonNode task = json.get("technical_task");

            if (task != null) {
                if (task.has("title") && !task.get("title").asText().isEmpty()) {
                    structuredSummary.append("📌 ").append(task.get("title").asText()).append("\n\n");
                }
                if (task.has("description") && !task.get("description").asText().isEmpty()) {
                    structuredSummary.append("📝 Описание:\n").append(task.get("description").asText()).append("\n\n");
                }
                if (task.has("requirements") && task.get("requirements").isArray()) {
                    structuredSummary.append("✅ Требования:\n");
                    for (JsonNode req : task.get("requirements")) {
                        structuredSummary.append("  • ").append(req.asText()).append("\n");
                    }
                    structuredSummary.append("\n");
                }
                if (task.has("deadline_estimate") && !task.get("deadline_estimate").asText().isEmpty()) {
                    structuredSummary.append("⏰ Срок: ").append(task.get("deadline_estimate").asText()).append("\n\n");
                }
                if (task.has("additional_notes") && !task.get("additional_notes").asText().isEmpty()) {
                    structuredSummary.append("💡 Примечание: ").append(task.get("additional_notes").asText());
                }
            }

            // Если ТЗ пустое, добавляем исходный текст
            if (structuredSummary.length() == 0 && originalText != null) {
                structuredSummary.append("📝 ").append(originalText);
            }

            ClassificationResult result = new ClassificationResult();
            result.setCategory(finalCategoryCode);
            result.setCategoryName(finalCategoryName);
            result.setSummary(structuredSummary.toString());
            result.setAssignedEmail(assignedEmail);
            result.setConfidenceScore(0.95);

            System.out.println("GigaChat классификация: " + finalCategoryName + " (код: " + finalCategoryCode + ")");
            System.out.println("Email отдела: " + assignedEmail);
            System.out.println("Сформированное ТЗ:\n" + structuredSummary);

            return result;

        } catch (Exception e) {
            System.err.println("Ошибка парсинга ответа GigaChat: " + e.getMessage());
            e.printStackTrace();
            return fallbackClassification(originalText);
        }
    }

    private Category findCategoryByCode(String code) {
        if (code == null || code.isEmpty()) return null;

        List<Category> categories = getCategories();
        return categories.stream()
                .filter(cat -> code.equalsIgnoreCase(cat.getCode()))
                .findFirst()
                .orElse(null);
    }

    private Category findCategoryByName(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) return null;

        List<Category> categories = getCategories();
        return categories.stream()
                .filter(cat -> cat.getName().equalsIgnoreCase(categoryName.trim()))
                .findFirst()
                .orElse(null);
    }

    private Category findSimilarCategory(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) return null;

        List<Category> categories = getCategories();
        String lowerSearch = categoryName.toLowerCase().trim();

        // Поиск по частичному совпадению
        Category exactMatch = categories.stream()
                .filter(cat -> lowerSearch.contains(cat.getName().toLowerCase()) ||
                        cat.getName().toLowerCase().contains(lowerSearch))
                .findFirst()
                .orElse(null);

        if (exactMatch != null) {
            return exactMatch;
        }

        // Поиск по ключевым словам
        String[] searchWords = lowerSearch.split("\\s+");
        for (Category cat : categories) {
            String catLower = cat.getName().toLowerCase();
            for (String word : searchWords) {
                if (word.length() > 2 && catLower.contains(word)) {
                    return cat;
                }
            }
        }

        return null;
    }

    private String findEmailByCategory(Category category) {
        if (category != null) {
            Department department = category.getDepartment();
            if (department != null && department.getEmailAddress() != null && !department.getEmailAddress().isEmpty()) {
                System.out.println("Найден email для отдела " + department.getName() + ": " + department.getEmailAddress());
                return department.getEmailAddress();
            } else if (department != null) {
                System.err.println("У отдела " + department.getName() + " не указан email");
            }
        }
        return getDefaultEmail();
    }

    private String getDefaultEmail() {
        // Получаем первый отдел из БД как дефолтный
        List<Department> departments = getDepartments();
        Department firstDeptWithEmail = departments.stream()
                .filter(dept -> dept.getEmailAddress() != null && !dept.getEmailAddress().isEmpty())
                .findFirst()
                .orElse(null);

        if (firstDeptWithEmail != null) {
            System.out.println("Используем дефолтный email отдела: " + firstDeptWithEmail.getEmailAddress());
            return firstDeptWithEmail.getEmailAddress();
        }

        System.err.println("В БД нет отделов с email! Используем заглушку.");
        return "default@company.com";
    }

    private ClassificationResult fallbackClassification(String text) {
        String lowerText = text != null ? text.toLowerCase() : "";
        List<Category> categories = getCategories();

        if (categories.isEmpty()) {
            // Если нет категорий в БД, возвращаем дефолтный результат
            ClassificationResult result = new ClassificationResult();
            result.setCategory("other");
            result.setCategoryName("Другое");
            result.setSummary(text != null && !text.isEmpty() ?
                    (text.length() <= 200 ? text : text.substring(0, 197) + "...") : "Нет описания");
            result.setAssignedEmail(getDefaultEmail());
            result.setConfidenceScore(0.50);
            return result;
        }

        Category matchedCategory = null;
        int maxMatches = 0;

        // Поиск категории по ключевым словам в названии и описании
        for (Category category : categories) {
            String categoryLower = category.getName().toLowerCase();
            int matches = 0;

            // Разбиваем название категории на ключевые слова
            String[] keywords = categoryLower.split("\\s+");
            for (String keyword : keywords) {
                if (keyword.length() > 2 && lowerText.contains(keyword)) {
                    matches++;
                }
            }

            // Также проверяем описание категории
            if (category.getDescription() != null) {
                String descLower = category.getDescription().toLowerCase();
                String[] descWords = descLower.split("\\s+");
                for (String word : descWords) {
                    if (word.length() > 3 && lowerText.contains(word)) {
                        matches++;
                    }
                }
            }

            if (matches > maxMatches) {
                maxMatches = matches;
                matchedCategory = category;
            }
        }

        if (matchedCategory != null && maxMatches > 0) {
            Department department = matchedCategory.getDepartment();
            ClassificationResult result = new ClassificationResult();
            result.setCategory(matchedCategory.getCode());
            result.setCategoryName(matchedCategory.getName());
            result.setSummary(text != null && !text.isEmpty() ?
                    (text.length() <= 200 ? text : text.substring(0, 197) + "...") : "Нет текста");
            result.setAssignedEmail(department != null ? department.getEmailAddress() : getDefaultEmail());
            result.setConfidenceScore(0.70);

            System.out.println("Fallback классификация: " + matchedCategory.getName() +
                    " (код: " + matchedCategory.getCode() + ", совпадений: " + maxMatches + ")");

            return result;
        }

        // Если категория не найдена, берем первую категорию из БД
        Category defaultCategory = categories.get(0);
        Department defaultDept = defaultCategory.getDepartment();

        ClassificationResult result = new ClassificationResult();
        result.setCategory(defaultCategory.getCode());
        result.setCategoryName(defaultCategory.getName());
        result.setSummary(text != null && !text.isEmpty() ?
                (text.length() <= 200 ? text : text.substring(0, 197) + "...") : "Нет описания");
        result.setAssignedEmail(defaultDept != null ? defaultDept.getEmailAddress() : getDefaultEmail());
        result.setConfidenceScore(0.60);

        System.out.println("Используем категорию по умолчанию: " + defaultCategory.getName() +
                " (код: " + defaultCategory.getCode() + ")");

        return result;
    }
}