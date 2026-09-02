package com.alphabetz.webalphabetz.service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.model.MatriculaDocumento;
import com.alphabetz.webalphabetz.repository.MatriculaDocumentoRepository;

@Service
public class MatriculaDocumentoService {

    public static final String VALOR_ANUIDADE = "valor-anuidade";
    public static final String CONTRATO_EDUCACIONAL = "contrato-educacional";
    public static final String HORARIO_PERSONALIZADO = "horario-personalizado";
    public static final String MATERIAL_ESCOLAR_BABY = "material-escolar-baby";
    public static final String MATERIAL_ESCOLAR_TURMAS = "material-escolar-turmas";

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            VALOR_ANUIDADE,
            CONTRATO_EDUCACIONAL,
            HORARIO_PERSONALIZADO,
            MATERIAL_ESCOLAR_BABY,
            MATERIAL_ESCOLAR_TURMAS);

    private final MatriculaDocumentoRepository repository;

    public MatriculaDocumentoService(MatriculaDocumentoRepository repository) {
        this.repository = repository;
    }

    public Map<String, MatriculaDocumento> getDocumentosMetadata() {
        Map<String, MatriculaDocumento> documentos = new LinkedHashMap<>();
        repository.findAllMetadata().forEach(documento -> documentos.put(documento.getTipo(), documento));
        return documentos;
    }

    public MatriculaDocumento getDocumento(String tipo) {
        validarTipo(tipo);
        return repository.findByTipo(tipo)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado."));
    }

    @Transactional
    public void atualizarDocumentos(
            MultipartFile valorAnuidade,
            MultipartFile contratoEducacional,
            MultipartFile horarioPersonalizado,
            MultipartFile materialEscolarBaby,
            MultipartFile materialEscolarTurmas) {

        boolean possuiArquivo = possuiArquivo(valorAnuidade)
                || possuiArquivo(contratoEducacional)
                || possuiArquivo(horarioPersonalizado)
                || possuiArquivo(materialEscolarBaby)
                || possuiArquivo(materialEscolarTurmas);

        if (!possuiArquivo) {
            throw new IllegalArgumentException("Selecione ao menos um PDF para atualizar.");
        }

        salvarSeEnviado(VALOR_ANUIDADE, valorAnuidade);
        salvarSeEnviado(CONTRATO_EDUCACIONAL, contratoEducacional);
        salvarSeEnviado(HORARIO_PERSONALIZADO, horarioPersonalizado);
        salvarSeEnviado(MATERIAL_ESCOLAR_BABY, materialEscolarBaby);
        salvarSeEnviado(MATERIAL_ESCOLAR_TURMAS, materialEscolarTurmas);
    }

    private void salvarSeEnviado(String tipo, MultipartFile arquivo) {
        if (!possuiArquivo(arquivo)) {
            return;
        }

        ArquivoPdf pdf = validarPdf(arquivo);

        try {
            MatriculaDocumento documento = repository.findByTipo(tipo).orElseGet(() -> novoDocumento(tipo));
            documento.setNomeArquivo(pdf.nome());
            documento.setTipoConteudo("application/pdf");
            documento.setTamanho(pdf.conteudo().length);
            documento.setConteudo(pdf.conteudo());
            repository.save(documento);
        } catch (DataAccessException exception) {
            throw new IllegalStateException("Não foi possível salvar os documentos de matrícula.", exception);
        }
    }

    private MatriculaDocumento novoDocumento(String tipo) {
        MatriculaDocumento documento = new MatriculaDocumento();
        documento.setId(UUID.randomUUID());
        documento.setTipo(tipo);
        documento.setNew(true);
        return documento;
    }

    private ArquivoPdf validarPdf(MultipartFile arquivo) {
        if (arquivo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Cada PDF deve ter no máximo 10 MB.");
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

    private void validarTipo(String tipo) {
        if (!TIPOS_PERMITIDOS.contains(tipo)) {
            throw new IllegalArgumentException("Tipo de documento inválido.");
        }
    }

    private record ArquivoPdf(String nome, byte[] conteudo) {
    }
}
