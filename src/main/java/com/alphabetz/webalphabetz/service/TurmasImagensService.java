package com.alphabetz.webalphabetz.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.model.TurmasImagens;
import com.alphabetz.webalphabetz.repository.TurmasImagensRepository;
import com.alphabetz.webalphabetz.utils.ValidationUtils;

@Service
public class TurmasImagensService {

    private final TurmasImagensRepository turmasImagensRepository;
    private final FileUploadService fileUploadService;

    public TurmasImagensService(TurmasImagensRepository turmasImagensRepository,
            FileUploadService fileUploadService) {
        this.turmasImagensRepository = turmasImagensRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional
    public TurmasImagens create(String nome, MultipartFile imageFile) {
        ValidationUtils.validarCampoStringObrigatorio(nome, "nome");
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("A imagem é obrigatória.");
        }

        String imageUrl = fileUploadService.salvarImagem(imageFile);
        try {
            TurmasImagens turma = new TurmasImagens();
            turma.setId(UUID.randomUUID());
            turma.setNome(nome.trim());
            turma.setImagemUrl(imageUrl);
            turma.setNew(true);
            return turmasImagensRepository.save(turma);
        } catch (RuntimeException exception) {
            fileUploadService.removerImagem(imageUrl);
            throw exception;
        }
    }

    @Transactional
    public TurmasImagens update(UUID id, String nome, MultipartFile imageFile) {
        ValidationUtils.validarCampoStringObrigatorio(nome, "nome");
        TurmasImagens turma = getById(id);
        turma.setNome(nome.trim());

        if (imageFile == null || imageFile.isEmpty()) {
            return turmasImagensRepository.save(turma);
        }

        String oldImageUrl = turma.getImagemUrl();
        String newImageUrl = fileUploadService.salvarImagem(imageFile);
        try {
            turma.setImagemUrl(newImageUrl);
            TurmasImagens updatedTurma = turmasImagensRepository.save(turma);
            if (oldImageUrl != null && !oldImageUrl.isBlank()) {
                fileUploadService.removerImagem(oldImageUrl);
            }
            return updatedTurma;
        } catch (RuntimeException exception) {
            fileUploadService.removerImagem(newImageUrl);
            throw exception;
        }
    }

    @Transactional
    public void delete(UUID id) {
        TurmasImagens turma = getById(id);
        String imageUrl = turma.getImagemUrl();
        turmasImagensRepository.delete(turma);
        if (imageUrl != null && !imageUrl.isBlank()) {
            fileUploadService.removerImagem(imageUrl);
        }
    }

    public TurmasImagens getById(UUID id) {
        return turmasImagensRepository.findById(Objects.requireNonNull(id, "id"))
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada."));
    }

    public List<TurmasImagens> getAll() {
        return turmasImagensRepository.findAllByOrderByNomeAsc();
    }
}
