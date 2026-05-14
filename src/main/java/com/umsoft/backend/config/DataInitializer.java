package com.umsoft.backend.config;

import com.umsoft.backend.entities.Category;
import com.umsoft.backend.entities.Department;
import com.umsoft.backend.repositories.CategoryRepository;
import com.umsoft.backend.repositories.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (departmentRepository.count() == 0) {
            System.out.println("Инициализация тестовых данных...");

            Department mobileDept = departmentRepository.create(new Department(
                    "Отдел мобильной разработки",
                    "Разработка мобильных приложений под iOS и Android",
                    "mobile-UmSoft@yandex.ru"
            ));

            Department webDept = departmentRepository.create(new Department(
                    "Отдел веб-разработки",
                    "Разработка веб-приложений и сайтов",
                    "web_umsoft@rambler.ru"
            ));

            Department integrationDept = departmentRepository.create(new Department(
                    "Отдел внедрения ПО",
                    "Внедрение ПО",
                    "integration_umsoft@mail.ru"
            ));

            Department otherDept = departmentRepository.create(new Department(
                    "Общий отдел",
                    "Общие вопросы",
                    "info-UmSoft@yandex.ru"
            ));

            categoryRepository.create(new Category(
                    "Мобильная разработка",
                    "mobile_development",
                    "Запросы по разработке мобильных приложений под iOS и Android",
                    mobileDept
            ));

            categoryRepository.create(new Category(
                    "Веб-разработка",
                    "web_development",
                    "Запросы по разработке веб-приложений и сайтов",
                    webDept
            ));

            categoryRepository.create(new Category(
                    "Системная интеграция",
                    "system_integration",
                    "Запросы по интеграции систем и внедрению ПО",
                    integrationDept
            ));

            categoryRepository.create(new Category(
                    "Другое",
                    "other",
                    "Прочие запросы, не попавшие в другие категории",
                    otherDept
            ));

            System.out.println("Тестовые данные успешно загружены!");
            System.out.println("Категории с кодами:");
            categoryRepository.findAll().forEach(cat ->
                    System.out.println("  - " + cat.getName() + " (код: " + cat.getCode() + ")")
            );
        }
    }
}