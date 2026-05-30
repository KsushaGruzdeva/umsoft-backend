package com.umsoft.backend.services;

import com.umsoft.backend.dtos.ClassificationResult;
import com.umsoft.backend.entities.Category;
import com.umsoft.backend.entities.Department;
import com.umsoft.backend.repositories.CategoryRepository;
import com.umsoft.backend.repositories.DepartmentRepository;
import com.umsoft.backend.services.impl.GigaChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование резервной классификации GigaChatService")
public class GigaChatServiceFallbackTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private GigaChatServiceImpl gigaChatService;

    private List<Category> testCategories;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        testDepartment = new Department();
        testDepartment.setId(1L);
        testDepartment.setName("Отдел разработки");
        testDepartment.setEmailAddress("dev@umsoft.ru");

        Category mobileCategory = new Category();
        mobileCategory.setId(1L);
        mobileCategory.setCode("MOB");
        mobileCategory.setName("Мобильная разработка");
        mobileCategory.setDescription("Разработка мобильных приложений");
        mobileCategory.setDepartment(testDepartment);

        Category webCategory = new Category();
        webCategory.setId(2L);
        webCategory.setCode("WEB");
        webCategory.setName("Веб-разработка");
        webCategory.setDescription("Создание веб-приложений");
        webCategory.setDepartment(testDepartment);

        Category integrationCategory = new Category();
        integrationCategory.setId(3L);
        integrationCategory.setCode("INT");
        integrationCategory.setName("Системная интеграция");
        integrationCategory.setDescription("Интеграция 1С, CRM");
        integrationCategory.setDepartment(testDepartment);

        testCategories = Arrays.asList(mobileCategory, webCategory, integrationCategory);
    }

    @Test
    @DisplayName("При недоступности GigaChat API должна сработать резервная классификация")
    void testFallbackClassification_WhenApiIsDown() {
        when(categoryRepository.findAll()).thenReturn(testCategories);

        String requestText = "Нужно разработать мобильное приложение для склада";

        ClassificationResult result = gigaChatService.classifyAndSummarize(requestText);

        assertNotNull(result, "Результат классификации не должен быть null");
        assertNotNull(result.getCategory(), "Категория должна быть определена");
        assertNotNull(result.getSummary(), "Саммари не должно быть null");
        assertTrue(result.getConfidenceScore() >= 0, "Уверенность должна быть неотрицательной");
    }

    @Test
    @DisplayName("Резервная классификация должна определить категорию по ключевым словам")
    void testFallbackClassification_DetectsByKeywords() {
        when(categoryRepository.findAll()).thenReturn(testCategories);

        String requestText = "Требуется сайт-визитка для компании с каталогом товаров";

        ClassificationResult result = gigaChatService.classifyAndSummarize(requestText);

        assertNotNull(result);
        assertNotNull(result.getCategory());
        assertNotNull(result.getCategoryName());
    }
}