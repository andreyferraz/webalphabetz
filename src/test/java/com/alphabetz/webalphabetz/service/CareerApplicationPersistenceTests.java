package com.alphabetz.webalphabetz.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import com.alphabetz.webalphabetz.model.CareerApplication;
import com.alphabetz.webalphabetz.repository.CareerApplicationRepository;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:target/career-applications-test.db")
class CareerApplicationPersistenceTests {

    @Autowired
    private CareerApplicationService service;

    @Autowired
    private CareerApplicationRepository repository;

    @BeforeEach
    void clearApplications() {
        repository.deleteAll();
    }

    @Test
    void storesAllApplicationDataAndTheOriginalResume() {
        byte[] resumeContent = "conteudo-pdf".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile resume = new MockMultipartFile(
                "curriculo", "curriculo-maria.pdf", "application/pdf", resumeContent);

        CareerApplication saved = service.registerApplication(
                "Maria Silva",
                "maria@example.com",
                "(27) 99999-9999",
                "Pedagógica",
                "Pedagogia",
                "Integral",
                "Cinco anos de experiência.",
                "https://linkedin.com/in/maria",
                true,
                resume);

        CareerApplication persisted = repository.findById(saved.getId()).orElseThrow();
        assertThat(persisted.getNome()).isEqualTo("Maria Silva");
        assertThat(persisted.getEmail()).isEqualTo("maria@example.com");
        assertThat(persisted.getTelefone()).isEqualTo("(27) 99999-9999");
        assertThat(persisted.getArea()).isEqualTo("Pedagógica");
        assertThat(persisted.getFormacao()).isEqualTo("Pedagogia");
        assertThat(persisted.getDisponibilidade()).isEqualTo("Integral");
        assertThat(persisted.getExperiencia()).isEqualTo("Cinco anos de experiência.");
        assertThat(persisted.getLinkedin()).isEqualTo("https://linkedin.com/in/maria");
        assertThat(persisted.isConsentimento()).isTrue();
        assertThat(persisted.getEnviadoEm()).isNotNull();
        assertThat(persisted.getCurriculoNome()).isEqualTo("curriculo-maria.pdf");
        assertThat(persisted.getCurriculoTipo()).isEqualTo("application/pdf");
        assertThat(persisted.getCurriculoTamanho()).isEqualTo(resumeContent.length);
        assertThat(persisted.getCurriculoConteudo()).isEqualTo(resumeContent);
    }

    @Test
    void listsNewestApplicationsWithoutLoadingResumeContent() {
        service.registerApplication(
                "Primeira Pessoa", "primeira@example.com", "27999999991", "Apoio",
                "", "", "", "", true,
                new MockMultipartFile("curriculo", "primeira.pdf", "application/pdf", new byte[] { 1 }));
        service.registerApplication(
                "Segunda Pessoa", "segunda@example.com", "27999999992", "Administrativa",
                "", "", "", "", true,
                new MockMultipartFile("curriculo", "segunda.pdf", "application/pdf", new byte[] { 2 }));

        assertThat(service.getAllApplications())
                .extracting(CareerApplication::getNome)
                .containsExactly("Segunda Pessoa", "Primeira Pessoa");
        assertThat(service.getAllApplications())
                .allSatisfy(application -> assertThat(application.getCurriculoConteudo()).isNull());
    }
}
