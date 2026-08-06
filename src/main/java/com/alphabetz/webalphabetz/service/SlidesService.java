package com.alphabetz.webalphabetz.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.model.SlideImage;
import com.alphabetz.webalphabetz.model.Slides;
import com.alphabetz.webalphabetz.repository.SlideImageRepository;
import com.alphabetz.webalphabetz.repository.SlidesRepository;
import com.alphabetz.webalphabetz.utils.ValidationUtils;

@Service
public class SlidesService {

    private final SlidesRepository slideRepository;
    private final SlideImageRepository slideImageRepository;
    private final FileUploadService fileUploadService;

    public SlidesService(SlidesRepository slideRepository,
            SlideImageRepository slideImageRepository,
            FileUploadService fileUploadService) {
        this.slideRepository = slideRepository;
        this.slideImageRepository = slideImageRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional
    public Slides createSlide(String titulo, List<MultipartFile> imageFiles) {
        ValidationUtils.validarCampoStringObrigatorio(titulo, "titulo");
        List<MultipartFile> validImages = validImages(imageFiles);
        ValidationUtils.validarListaObrigatoria(validImages, "fotos");

        List<String> savedFiles = saveImages(validImages);
        try {
            Slides slide = new Slides();
            slide.setId(UUID.randomUUID());
            slide.setTitulo(titulo.trim());
            slide.setImagemUrl(savedFiles.get(0));
            slide.setNew(true);
            slideRepository.save(slide);

            for (int index = 0; index < savedFiles.size(); index++) {
                slideImageRepository.save(newImage(slide.getId(), savedFiles.get(index), index));
            }

            return loadImages(slide);
        } catch (RuntimeException exception) {
            removeFiles(savedFiles);
            throw exception;
        }
    }

    @Transactional
    public Slides updateSlide(UUID id, String titulo, List<MultipartFile> imageFiles,
            List<UUID> removedImageIds) {
        UUID slideId = Objects.requireNonNull(id, "id");
        ValidationUtils.validarCampoStringObrigatorio(titulo, "titulo");

        Slides slide = slideRepository.findById(slideId)
                .orElseThrow(() -> new IllegalArgumentException("Slide não encontrado."));
        List<SlideImage> currentImages = slideImageRepository.findAllBySlideIdOrderByOrdemAsc(slideId);
        Set<UUID> idsToRemove = removedImageIds == null
                ? Set.of()
                : new HashSet<>(removedImageIds);

        Set<UUID> currentIds = new HashSet<>();
        currentImages.forEach(image -> currentIds.add(image.getId()));
        if (!currentIds.containsAll(idsToRemove)) {
            throw new IllegalArgumentException("Uma das fotos selecionadas não pertence a este slide.");
        }

        List<SlideImage> remainingImages = currentImages.stream()
                .filter(image -> !idsToRemove.contains(image.getId()))
                .toList();
        List<MultipartFile> validNewImages = validImages(imageFiles);
        if (remainingImages.isEmpty() && validNewImages.isEmpty()) {
            throw new IllegalArgumentException("O slide precisa ter pelo menos uma foto.");
        }

        List<String> savedFiles = saveImages(validNewImages);
        try {
            for (SlideImage image : currentImages) {
                if (idsToRemove.contains(image.getId())) {
                    slideImageRepository.deleteById(image.getId());
                }
            }

            int nextOrder = currentImages.stream()
                    .map(SlideImage::getOrdem)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(-1) + 1;
            for (String savedFile : savedFiles) {
                slideImageRepository.save(newImage(slideId, savedFile, nextOrder++));
            }

            slide.setTitulo(titulo.trim());
            slide.setImagemUrl(remainingImages.isEmpty()
                    ? savedFiles.get(0)
                    : remainingImages.get(0).getImagemUrl());
            slide.setNew(false);
            slideRepository.save(slide);

            currentImages.stream()
                    .filter(image -> idsToRemove.contains(image.getId()))
                    .map(SlideImage::getImagemUrl)
                    .forEach(fileUploadService::removerImagem);

            return loadImages(slide);
        } catch (RuntimeException exception) {
            removeFiles(savedFiles);
            throw exception;
        }
    }

    @Transactional
    public void deleteSlide(UUID id) {
        UUID slideId = Objects.requireNonNull(id, "id");
        Slides slide = slideRepository.findById(slideId)
                .orElseThrow(() -> new IllegalArgumentException("Slide não encontrado."));
        List<SlideImage> images = slideImageRepository.findAllBySlideIdOrderByOrdemAsc(slideId);

        slideImageRepository.deleteAllBySlideId(slideId);
        slideRepository.deleteById(slideId);

        Set<String> files = new LinkedHashSet<>();
        images.stream().map(SlideImage::getImagemUrl).forEach(files::add);
        files.add(slide.getImagemUrl());
        removeFiles(files);
    }

    public List<Slides> getAllSlides() {
        List<Slides> slides = slideRepository.findAllByOrderByTituloAsc();
        slides.forEach(this::loadImages);
        return slides;
    }

    public long getTotalImages(List<Slides> slides) {
        return slides.stream().mapToLong(Slides::getQuantidadeFotos).sum();
    }

    private Slides loadImages(Slides slide) {
        List<SlideImage> images = slideImageRepository.findAllBySlideIdOrderByOrdemAsc(slide.getId());
        if (images.isEmpty() && slide.getImagemUrl() != null && !slide.getImagemUrl().isBlank()) {
            SlideImage legacyImage = new SlideImage();
            legacyImage.setSlideId(slide.getId());
            legacyImage.setImagemUrl(slide.getImagemUrl());
            legacyImage.setOrdem(0);
            images = List.of(legacyImage);
        }
        slide.setImagens(new ArrayList<>(images));
        return slide;
    }

    private SlideImage newImage(UUID slideId, String imageUrl, int order) {
        SlideImage image = new SlideImage();
        image.setId(UUID.randomUUID());
        image.setSlideId(slideId);
        image.setImagemUrl(imageUrl);
        image.setOrdem(order);
        image.setNew(true);
        return image;
    }

    private List<MultipartFile> validImages(List<MultipartFile> images) {
        if (images == null) {
            return List.of();
        }
        return images.stream()
                .filter(Objects::nonNull)
                .filter(image -> !image.isEmpty())
                .toList();
    }

    private List<String> saveImages(List<MultipartFile> images) {
        List<String> savedFiles = new ArrayList<>();
        try {
            for (MultipartFile image : images) {
                savedFiles.add(fileUploadService.salvarImagem(image));
            }
            return savedFiles;
        } catch (RuntimeException exception) {
            removeFiles(savedFiles);
            throw exception;
        }
    }

    private void removeFiles(Iterable<String> files) {
        for (String file : files) {
            if (file != null && !file.isBlank()) {
                fileUploadService.removerImagem(file);
            }
        }
    }
}
