package com.alphabetz.webalphabetz.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.alphabetz.webalphabetz.model.DashboardSummary;
import com.alphabetz.webalphabetz.repository.AdminRepository;
import com.alphabetz.webalphabetz.repository.BlogRepository;
import com.alphabetz.webalphabetz.repository.BlogCategoryRepository;
import com.alphabetz.webalphabetz.repository.CareerApplicationRepository;
import com.alphabetz.webalphabetz.repository.SlideImageRepository;
import com.alphabetz.webalphabetz.repository.SlidesRepository;

@Service
public class DashboardService {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
            .ofPattern("dd 'de' MMMM", Locale.forLanguageTag("pt-BR"));

    private final SlidesRepository slidesRepository;
    private final SlideImageRepository slideImageRepository;
    private final BlogRepository blogRepository;
    private final BlogCategoryRepository blogCategoryRepository;
    private final CareerApplicationRepository careerApplicationRepository;
    private final AdminRepository adminRepository;

    public DashboardService(SlidesRepository slidesRepository,
            SlideImageRepository slideImageRepository,
            BlogRepository blogRepository,
            BlogCategoryRepository blogCategoryRepository,
            CareerApplicationRepository careerApplicationRepository,
            AdminRepository adminRepository) {
        this.slidesRepository = slidesRepository;
        this.slideImageRepository = slideImageRepository;
        this.blogRepository = blogRepository;
        this.blogCategoryRepository = blogCategoryRepository;
        this.careerApplicationRepository = careerApplicationRepository;
        this.adminRepository = adminRepository;
    }

    public DashboardSummary getSummary() {
        return new DashboardSummary(
                slidesRepository.count(),
                slideImageRepository.count(),
                blogRepository.count(),
                blogCategoryRepository.count(),
                careerApplicationRepository.count(),
                adminRepository.count(),
                LocalDate.now(SAO_PAULO).format(DATE_FORMAT));
    }
}
