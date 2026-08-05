package com.alphabetz.webalphabetz.service;

import java.util.UUID;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.model.SlideAbordagemCuida;
import com.alphabetz.webalphabetz.repository.SlideAbordagemCuidaRepository;
import com.alphabetz.webalphabetz.utils.ValidationUtils;

@Service
public class SlideAbordagemCuidaService {

    private final SlideAbordagemCuidaRepository slideAbordagemCuidaRepository;
    private final FileUploadService fileUploadService;

    public SlideAbordagemCuidaService(SlideAbordagemCuidaRepository slideAbordagemCuidaRepository, FileUploadService fileUploadService) {
        this.slideAbordagemCuidaRepository = slideAbordagemCuidaRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional
    public SlideAbordagemCuida createSlide(SlideAbordagemCuida slide, MultipartFile imageFile){
        ValidationUtils.validarCampoObrigatorio(slide, "slide");
        if(imageFile != null && !imageFile.isEmpty()){
            String imageUrl = fileUploadService.salvarImagem(imageFile);
            slide.setImagemUrl(imageUrl);
        } else {
            ValidationUtils.validarCampoStringObrigatorio(slide.getImagemUrl(), "imagemUrl");
        }
        if(slide.getId() == null){
            slide.setId(UUID.randomUUID());
        }
        slide.setNew(true);
        return slideAbordagemCuidaRepository.save(slide);
    }

    @Transactional
    public SlideAbordagemCuida updateSlide(SlideAbordagemCuida slide, MultipartFile imageFile){
        ValidationUtils.validarCampoObrigatorio(slide, "slide");
        UUID id = Objects.requireNonNull(slide.getId(), "id");

        SlideAbordagemCuida existingSlide = slideAbordagemCuidaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Slide não encontrado."));

        if(imageFile != null && !imageFile.isEmpty()){
            if(existingSlide.getImagemUrl() != null && !existingSlide.getImagemUrl().isEmpty()){
                fileUploadService.removerImagem(existingSlide.getImagemUrl());
            }
            String imageUrl = fileUploadService.salvarImagem(imageFile);
            slide.setImagemUrl(imageUrl);
        } else if(slide.getImagemUrl() == null || slide.getImagemUrl().isEmpty()){
            slide.setImagemUrl(existingSlide.getImagemUrl());
        }
        slide.setNew(false);
        return slideAbordagemCuidaRepository.save(slide);
    }

    @Transactional
    public void deleteSlide(UUID id){
        UUID slideId = Objects.requireNonNull(id, "id");

        SlideAbordagemCuida existingSlide = slideAbordagemCuidaRepository.findById(slideId)
                .orElseThrow(() -> new IllegalArgumentException("Slide não encontrado."));

        if(existingSlide.getImagemUrl() != null && !existingSlide.getImagemUrl().isEmpty()){
            fileUploadService.removerImagem(existingSlide.getImagemUrl());
        }
        slideAbordagemCuidaRepository.deleteById(slideId);
    }

    public Iterable<SlideAbordagemCuida> getAllSlides(){
        return slideAbordagemCuidaRepository.findAll();
    }

}
