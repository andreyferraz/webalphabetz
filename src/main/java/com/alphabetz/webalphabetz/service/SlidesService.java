package com.alphabetz.webalphabetz.service;

import java.util.UUID;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.model.Slides;
import com.alphabetz.webalphabetz.repository.SlidesRepository;
import com.alphabetz.webalphabetz.utils.ValidationUtils;

@Service
public class SlidesService {

    private final SlidesRepository slideRepository;
    private final FileUploadService fileUploadService;

    public SlidesService(SlidesRepository slideRepository, FileUploadService fileUploadService) {
        this.slideRepository = slideRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional
    public Slides createSlide(Slides slide, MultipartFile imageFile){
        ValidationUtils.validarCampoObrigatorio(slide, "slide");
        ValidationUtils.validarCampoStringObrigatorio(slide.getTitulo(), "titulo");
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
        return slideRepository.save(slide);
    }

    @Transactional
    public Slides updateSlide(Slides slide, MultipartFile imageFile){
        ValidationUtils.validarCampoObrigatorio(slide, "slide");
        ValidationUtils.validarCampoStringObrigatorio(slide.getTitulo(), "titulo");
        UUID id = Objects.requireNonNull(slide.getId(), "id");

        Slides existingSlide = slideRepository.findById(id)
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
        return slideRepository.save(slide);
    }

    @Transactional
    public void deleteSlide(UUID id){
        UUID slideId = Objects.requireNonNull(id, "id");

        Slides existingSlide = slideRepository.findById(slideId)
                .orElseThrow(() -> new IllegalArgumentException("Slide não encontrado."));

        if(existingSlide.getImagemUrl() != null && !existingSlide.getImagemUrl().isEmpty()){
            fileUploadService.removerImagem(existingSlide.getImagemUrl());
        }
        slideRepository.deleteById(slideId);
    }

    public Iterable<Slides> getAllSlides(){
        return slideRepository.findAll();
    }

}
