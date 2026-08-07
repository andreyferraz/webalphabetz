package com.alphabetz.webalphabetz.service;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class CareerApplicationService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_FIELD_LENGTH = 2_000;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx");

    private final JavaMailSender mailSender;
    private final String senderEmail;
    private final String recipientEmail;

    public CareerApplicationService(JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String senderEmail,
            @Value("${app.career.recipient-email}") String recipientEmail) {
        this.mailSender = mailSender;
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
    }

    public void sendApplication(String nome, String email, String telefone, String area,
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
        if (!StringUtils.hasText(senderEmail)) {
            throw new IllegalStateException("O remetente SMTP não está configurado.");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(recipientEmail);
            helper.setReplyTo(safeEmail);
            helper.setSubject("Nova candidatura Alphabetz - " + safeName);
            helper.setText(buildMessage(safeName, safeEmail, safePhone, safeArea,
                    optional(formacao, false), optional(disponibilidade, false),
                    optional(experiencia, true), optional(linkedin, false)), false);
            helper.addAttachment(attachmentName, new ByteArrayResource(curriculo.getBytes()),
                    StringUtils.hasText(curriculo.getContentType())
                            ? curriculo.getContentType()
                            : "application/octet-stream");
            mailSender.send(message);
        } catch (MessagingException | IOException | MailException exception) {
            throw new IllegalStateException("Não foi possível enviar a candidatura agora.", exception);
        }
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

    private String buildMessage(String nome, String email, String telefone, String area,
            String formacao, String disponibilidade, String experiencia, String linkedin) {
        return """
                Nova candidatura recebida pelo site da Alphabetz

                Nome: %s
                E-mail: %s
                Telefone/WhatsApp: %s
                Área de interesse: %s
                Formação: %s
                Disponibilidade: %s
                LinkedIn ou portfólio: %s

                Resumo da experiência:
                %s

                O candidato autorizou o uso dos dados para participação em processos seletivos.
                """.formatted(
                nome,
                email,
                telefone,
                area,
                displayValue(formacao),
                displayValue(disponibilidade),
                displayValue(linkedin),
                displayValue(experiencia));
    }

    private String displayValue(String value) {
        return value.isBlank() ? "Não informado" : value;
    }
}
