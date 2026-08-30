package com.alphabetz.webalphabetz.service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.model.DocumentoInstitucional;
import com.alphabetz.webalphabetz.repository.DocumentoInstitucionalRepository;
import com.alphabetz.webalphabetz.utils.ValidationUtils;

@Service
public class DocumentoInstitucionalService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final DocumentoInstitucionalRepository repository;

    public DocumentoInstitucionalService(DocumentoInstitucionalRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DocumentoInstitucional create(String nome, MultipartFile arquivo) {
        ValidationUtils.validarCampoStringObrigatorio(nome, "nome");
        if (!possuiArquivo(arquivo)) {
            throw new IllegalArgumentException("O arquivo PDF é obrigatório.");
        }

        ArquivoPdf pdf = validarPdf(arquivo);
        DocumentoInstitucional documento = new DocumentoInstitucional();
        documento.setId(UUID.randomUUID());
        documento.setNome(nome.trim());
        documento.setNomeArquivo(pdf.nome());
        documento.setTipoConteudo("application/pdf");
        documento.setTamanho(pdf.conteudo().length);
        documento.setConteudo(pdf.conteudo());
        documento.setNew(true);

        try {
            return repository.save(documento);
        } catch (DataAccessException exception) {
            throw new IllegalStateException("Não foi possível salvar o documento institucional.", exception);
        }
    }

    @Transactional
    public DocumentoInstitucional update(UUID id, String nome, MultipartFile arquivo) {
        ValidationUtils.validarCampoStringObrigatorio(nome, "nome");
        DocumentoInstitucional documento = getById(id);
        documento.setNome(nome.trim());

        if (possuiArquivo(arquivo)) {
            ArquivoPdf pdf = validarPdf(arquivo);
            documento.setNomeArquivo(pdf.nome());
            documento.setTipoConteudo("application/pdf");
            documento.setTamanho(pdf.conteudo().length);
            documento.setConteudo(pdf.conteudo());
        }

        try {
            return repository.save(documento);
        } catch (DataAccessException exception) {
            throw new IllegalStateException("Não foi possível atualizar o documento institucional.", exception);
        }
    }

    @Transactional
    public void delete(UUID id) {
        DocumentoInstitucional documento = getById(id);
        repository.delete(documento);
    }

    public DocumentoInstitucional getById(UUID id) {
        return repository.findById(Objects.requireNonNull(id, "id"))
                .orElseThrow(() -> new IllegalArgumentException("Documento institucional não encontrado."));
    }

    public List<DocumentoInstitucional> getAllMetadata() {
        return repository.findAllMetadata();
    }

    private ArquivoPdf validarPdf(MultipartFile arquivo) {
        if (arquivo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("O PDF deve ter no máximo 10 MB.");
        }

        String nomeArquivo = StringUtils.cleanPath(
                arquivo.getOriginalFilename() == null ? "documento.pdf" : arquivo.getOriginalFilename());
        String extensao = StringUtils.getFilenameExtension(nomeArquivo);

        if (nomeArquivo.contains("..") || extensao == null || !"pdf".equals(extensao.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Envie somente arquivos PDF.");
        }

        try {
            byte[] conteudo = arquivo.getBytes();
            if (conteudo.length < 5
                    || conteudo[0] != '%'
                    || conteudo[1] != 'P'
                    || conteudo[2] != 'D'
                    || conteudo[3] != 'F'
                    || conteudo[4] != '-') {
                throw new IllegalArgumentException("O arquivo enviado não é um PDF válido.");
            }
            return new ArquivoPdf(nomeArquivo, conteudo);
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível processar o PDF.", exception);
        }
    }

    private boolean possuiArquivo(MultipartFile arquivo) {
        return arquivo != null && !arquivo.isEmpty();
    }

    private record ArquivoPdf(String nome, byte[] conteudo) {
    }
}
