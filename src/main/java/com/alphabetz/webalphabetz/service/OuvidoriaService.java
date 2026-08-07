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

import com.alphabetz.webalphabetz.model.OuvidoriaManifestacao;
import com.alphabetz.webalphabetz.repository.OuvidoriaManifestacaoRepository;

@Service
public class OuvidoriaService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int MAX_FIELD_LENGTH = 2_000;
    private static final int MAX_DETAILS_LENGTH = 10_000;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Set<String> SUBJECTS = Set.of(
            "Elogio", "Reclamação", "Denúncia", "Sugestão", "Crítica", "Solicitação");
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "txt", "jpg", "jpeg", "png", "webp");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final OuvidoriaManifestacaoRepository repository;

    public OuvidoriaService(OuvidoriaManifestacaoRepository repository) {
        this.repository = repository;
    }

    public OuvidoriaManifestacao register(String nome, String sobrenome, String email,
            String celular, String perfil, String areaAtuacao, String assunto, String detalhes,
            boolean receberRetorno, MultipartFile arquivo, MultipartFile documentoImagem) {
        String safeEmail = required(email, "e-mail", MAX_FIELD_LENGTH);
        String safeSubject = required(assunto, "assunto da Ouvidoria", MAX_FIELD_LENGTH);
        if (!EMAIL_PATTERN.matcher(safeEmail).matches()) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
        if (!SUBJECTS.contains(safeSubject)) {
            throw new IllegalArgumentException("Selecione um assunto válido.");
        }

        Attachment generalFile = attachment(arquivo, DOCUMENT_EXTENSIONS, "arquivo");
        Attachment documentImage = attachment(documentoImagem, IMAGE_EXTENSIONS, "imagem do documento");

        try {
            OuvidoriaManifestacao manifestation = new OuvidoriaManifestacao();
            manifestation.setId(UUID.randomUUID());
            manifestation.setNome(required(nome, "nome", MAX_FIELD_LENGTH));
            manifestation.setSobrenome(required(sobrenome, "sobrenome", MAX_FIELD_LENGTH));
            manifestation.setEmail(safeEmail);
            manifestation.setCelular(required(celular, "número de celular", MAX_FIELD_LENGTH));
            manifestation.setPerfil(required(perfil, "perfil", MAX_FIELD_LENGTH));
            manifestation.setAreaAtuacao(required(areaAtuacao, "área de atuação", MAX_FIELD_LENGTH));
            manifestation.setAssunto(safeSubject);
            manifestation.setDetalhes(required(detalhes, "detalhes", MAX_DETAILS_LENGTH));
            manifestation.setReceberRetorno(receberRetorno);
            manifestation.setEnviadoEm(LocalDateTime.now());
            manifestation.setArquivoNome(generalFile.name());
            manifestation.setArquivoTipo(generalFile.contentType());
            manifestation.setArquivoTamanho(generalFile.size());
            manifestation.setArquivoConteudo(generalFile.content());
            manifestation.setDocumentoImagemNome(documentImage.name());
            manifestation.setDocumentoImagemTipo(documentImage.contentType());
            manifestation.setDocumentoImagemTamanho(documentImage.size());
            manifestation.setDocumentoImagemConteudo(documentImage.content());
            manifestation.setNew(true);
            return repository.save(manifestation);
        } catch (DataAccessException exception) {
            throw new IllegalStateException("Não foi possível registrar a manifestação agora.", exception);
        }
    }

    public List<OuvidoriaManifestacao> getAll() {
        return repository.findAllNewestFirst();
    }

    public OuvidoriaManifestacao get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Manifestação não encontrada."));
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Manifestação não encontrada.");
        }
        repository.deleteById(id);
    }

    private String required(String value, String fieldName, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("O campo " + fieldName + " é obrigatório.");
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("O campo " + fieldName + " excedeu o tamanho permitido.");
        }
        return normalized.replace("\r", "");
    }

    private Attachment attachment(MultipartFile file, Set<String> extensions, String label) {
        if (file == null || file.isEmpty()) {
            return Attachment.empty();
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("O " + label + " deve ter até 10 MB.");
        }
        String name = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? label : file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(name);
        if (name.contains("..") || extension == null
                || !extensions.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Formato inválido para " + label + ".");
        }
        try {
            return new Attachment(name,
                    StringUtils.hasText(file.getContentType())
                            ? file.getContentType()
                            : "application/octet-stream",
                    file.getSize(), file.getBytes());
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível processar o " + label + ".", exception);
        }
    }

    private record Attachment(String name, String contentType, long size, byte[] content) {
        private static Attachment empty() {
            return new Attachment(null, null, 0, null);
        }
    }
}
