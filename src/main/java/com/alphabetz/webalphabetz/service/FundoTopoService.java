package com.alphabetz.webalphabetz.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.model.FundoTopo;
import com.alphabetz.webalphabetz.repository.FundoTopoRepository;
import com.alphabetz.webalphabetz.utils.ValidationUtils;

@Service
public class FundoTopoService {

    private final FundoTopoRepository fundoTopoRepository;
    private final FileUploadService fileUploadService;

    public FundoTopoService(FundoTopoRepository fundoTopoRepository, FileUploadService fileUploadService) {
        this.fundoTopoRepository = fundoTopoRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional
    public FundoTopo createFundoTopo(String nomePagina, MultipartFile imageFile) {
        ValidationUtils.validarCampoStringObrigatorio(nomePagina, "nomePagina");
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("A imagem é obrigatória.");
        }

        String imageUrl = fileUploadService.salvarImagem(imageFile);
        try {
            FundoTopo fundo = new FundoTopo();
            fundo.setId(UUID.randomUUID());
            fundo.setNomePagina(nomePagina.trim());
            fundo.setImagemUrl(imageUrl);
            fundo.setNew(true);
            return fundoTopoRepository.save(fundo);
        } catch (RuntimeException exception) {
            fileUploadService.removerImagem(imageUrl);
            throw exception;
        }
    }

    @Transactional
    public FundoTopo updateFundoTopo(UUID id, String nomePagina, MultipartFile imageFile) {
        ValidationUtils.validarCampoStringObrigatorio(nomePagina, "nomePagina");
        FundoTopo fundo = getById(id);
        fundo.setNomePagina(nomePagina.trim());

        if (imageFile == null || imageFile.isEmpty()) {
            return fundoTopoRepository.save(fundo);
        }

        String oldImageUrl = fundo.getImagemUrl();
        String newImageUrl = fileUploadService.salvarImagem(imageFile);
        try {
            fundo.setImagemUrl(newImageUrl);
            FundoTopo updatedFundo = fundoTopoRepository.save(fundo);
            if (oldImageUrl != null && !oldImageUrl.isBlank()) {
                fileUploadService.removerImagem(oldImageUrl);
            }
            return updatedFundo;
        } catch (RuntimeException exception) {
            fileUploadService.removerImagem(newImageUrl);
            throw exception;
        }
    }

    @Transactional
    public void deleteFundoTopo(UUID id) {
        FundoTopo fundo = getById(id);
        String imageUrl = fundo.getImagemUrl();
        fundoTopoRepository.delete(fundo);
        if (imageUrl != null && !imageUrl.isBlank()) {
            fileUploadService.removerImagem(imageUrl);
        }
    }

    public FundoTopo getById(UUID id) {
        return fundoTopoRepository.findById(Objects.requireNonNull(id, "id"))
                .orElseThrow(() -> new IllegalArgumentException("Fundo não encontrado."));
    }

    public List<FundoTopo> getAll() {
        return fundoTopoRepository.findAllByOrderByNomePaginaAsc();
    }

}
