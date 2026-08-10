package com.alphabetz.webalphabetz.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.model.Depoimentos;
import com.alphabetz.webalphabetz.repository.DepoimentosRepository;
import com.alphabetz.webalphabetz.utils.ValidationUtils;

@Service
public class DepoimentosService {

    private final DepoimentosRepository depoimentosRepository;
    private final FileUploadService fileUploadService;

    public DepoimentosService(DepoimentosRepository depoimentosRepository, FileUploadService fileUploadService) {
        this.depoimentosRepository = depoimentosRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional
    public Depoimentos createDepoimento(Depoimentos depoimento, MultipartFile imageFile) {
        ValidationUtils.validarCampoObrigatorio(depoimento,  "depoimento");
        ValidationUtils.validarCampoStringObrigatorio(depoimento.getNome(), "nome");
        ValidationUtils.validarCampoStringObrigatorio(depoimento.getDepoimento(), "depoimento");
        ValidationUtils.validarCampoObrigatorio(imageFile, "imagem");

        depoimento.setId(UUID.randomUUID());
        depoimento.setNew(true);

        String imageUrl = fileUploadService.salvarImagem(imageFile);
        try {
            depoimento.setImagemUrl(imageUrl);
            return depoimentosRepository.save(depoimento);
        } catch (RuntimeException exception) {
            fileUploadService.removerImagem(imageUrl);
            throw exception;
        }
    }

}
