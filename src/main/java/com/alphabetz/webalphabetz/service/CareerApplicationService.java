package com.alphabetz.webalphabetz.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.model.CareerApplication;
import com.alphabetz.webalphabetz.repository.CareerApplicationRepository;

@Service
public class CareerApplicationService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_FIELD_LENGTH = 2_000;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx");

    private final CareerApplicationRepository repository;

    public CareerApplicationService(CareerApplicationRepository repository) {
        this.repository = repository;
    }

    public CareerApplication registerApplication(String nome, String email, String telefone, String area,
            String formacao, String disponibilidade, String experiencia, String linkedin,
            boolean consentimento, MultipartFile curriculo) {
        String safeName = required(nome, "nome");
        String safeEmail = required(email, "e-mail");
        String safePhone = required(telefone, "telefone");
        String safeArea = required(area, "área de interesse");

        if (!EMAIL_PATTERN.matcher(safeEmail).matches()) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
        if (!consentimento) {
            throw new IllegalArgumentException("O consentimento para uso dos dados é obrigatório.");
        }

        String attachmentName = validateAttachment(curriculo);

        try {
            CareerApplication application = new CareerApplication();
            application.setId(UUID.randomUUID());
            application.setNome(safeName);
            application.setEmail(safeEmail);
            application.setTelefone(safePhone);
            application.setArea(safeArea);
            application.setFormacao(optional(formacao, false));
            application.setDisponibilidade(optional(disponibilidade, false));
            application.setExperiencia(optional(experiencia, true));
            application.setLinkedin(optional(linkedin, false));
            application.setConsentimento(true);
            application.setEnviadoEm(LocalDateTime.now());
            application.setCurriculoNome(attachmentName);
            application.setCurriculoTipo(StringUtils.hasText(curriculo.getContentType())
                    ? curriculo.getContentType()
                    : "application/octet-stream");
            application.setCurriculoTamanho(curriculo.getSize());
            application.setCurriculoConteudo(curriculo.getBytes());
            application.setNew(true);
            return repository.save(application);
        } catch (IOException | DataAccessException exception) {
            throw new IllegalStateException("Não foi possível registrar a candidatura agora.", exception);
        }
    }

    public List<CareerApplication> getAllApplications() {
        return repository.findAllNewestFirst();
    }

    public CareerApplication getApplication(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidatura não encontrada."));
    }

    public void deleteApplication(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Candidatura não encontrada.");
        }
        repository.deleteById(id);
    }

    private String required(String value, String fieldName) {
        String normalized = optional(value, false);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("O campo " + fieldName + " é obrigatório.");
        }
        return normalized;
    }

    private String optional(String value, boolean multiline) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_FIELD_LENGTH) {
            throw new IllegalArgumentException("Um dos campos excedeu o tamanho permitido.");
        }
        return multiline
                ? normalized.replace("\r", "")
                : normalized.replaceAll("[\\r\\n]+", " ");
    }

    private String validateAttachment(MultipartFile attachment) {
        if (attachment == null || attachment.isEmpty()) {
            throw new IllegalArgumentException("Anexe o currículo.");
        }
        if (attachment.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("O currículo deve ter até 5 MB.");
        }

        String fileName = StringUtils.cleanPath(
                attachment.getOriginalFilename() == null ? "curriculo" : attachment.getOriginalFilename());
        if (fileName.contains("..")) {
            throw new IllegalArgumentException("Nome de arquivo inválido.");
        }
        String extension = StringUtils.getFilenameExtension(fileName);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Envie o currículo em PDF, DOC ou DOCX.");
        }
        return fileName;
    }

}
