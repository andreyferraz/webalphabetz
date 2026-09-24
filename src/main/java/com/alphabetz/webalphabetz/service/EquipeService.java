package com.alphabetz.webalphabetz.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.model.CargoEquipe;
import com.alphabetz.webalphabetz.model.MembroEquipe;
import com.alphabetz.webalphabetz.repository.MembroEquipeRepository;
import com.alphabetz.webalphabetz.utils.ValidationUtils;

@Service
public class EquipeService {

    private static final Comparator<MembroEquipe> ORDEM_HIERARQUICA = Comparator
            .comparingInt((MembroEquipe membro) -> membro.getCargo().ordinal())
            .thenComparing(MembroEquipe::getNome, String.CASE_INSENSITIVE_ORDER);

    private final MembroEquipeRepository membroEquipeRepository;
    private final FileUploadService fileUploadService;

    public EquipeService(MembroEquipeRepository membroEquipeRepository, FileUploadService fileUploadService) {
        this.membroEquipeRepository = membroEquipeRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional
    public MembroEquipe create(String nome, CargoEquipe cargo, MultipartFile imagem) {
        validar(nome, cargo);
        if (imagem == null || imagem.isEmpty()) {
            throw new IllegalArgumentException("A foto é obrigatória.");
        }

        String imagemUrl = fileUploadService.salvarImagem(imagem);
        try {
            MembroEquipe membro = new MembroEquipe();
            membro.setId(UUID.randomUUID());
            membro.setNome(nome.trim());
            membro.setCargo(cargo);
            membro.setImagemUrl(imagemUrl);
            membro.setNew(true);
            return membroEquipeRepository.save(membro);
        } catch (RuntimeException exception) {
            fileUploadService.removerImagem(imagemUrl);
            throw exception;
        }
    }

    @Transactional
    public MembroEquipe update(UUID id, String nome, CargoEquipe cargo, MultipartFile imagem) {
        validar(nome, cargo);
        MembroEquipe membro = getById(id);
        membro.setNome(nome.trim());
        membro.setCargo(cargo);

        if (imagem == null || imagem.isEmpty()) {
            return membroEquipeRepository.save(membro);
        }

        String imagemAnterior = membro.getImagemUrl();
        String novaImagem = fileUploadService.salvarImagem(imagem);
        try {
            membro.setImagemUrl(novaImagem);
            MembroEquipe membroAtualizado = membroEquipeRepository.save(membro);
            if (imagemAnterior != null && !imagemAnterior.isBlank()) {
                fileUploadService.removerImagem(imagemAnterior);
            }
            return membroAtualizado;
        } catch (RuntimeException exception) {
            fileUploadService.removerImagem(novaImagem);
            throw exception;
        }
    }

    @Transactional
    public void delete(UUID id) {
        MembroEquipe membro = getById(id);
        String imagemUrl = membro.getImagemUrl();
        membroEquipeRepository.delete(membro);
        if (imagemUrl != null && !imagemUrl.isBlank()) {
            fileUploadService.removerImagem(imagemUrl);
        }
    }

    public MembroEquipe getById(UUID id) {
        return membroEquipeRepository.findById(Objects.requireNonNull(id, "id"))
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado."));
    }

    public List<MembroEquipe> getAll() {
        return StreamSupport.stream(membroEquipeRepository.findAll().spliterator(), false)
                .sorted(ORDEM_HIERARQUICA)
                .toList();
    }

    public Map<String, List<MembroEquipe>> getGroupedByCargo() {
        Map<String, List<MembroEquipe>> equipePorCargo = new LinkedHashMap<>();
        getAll().forEach(membro -> equipePorCargo
                .computeIfAbsent(getTituloGrupo(membro.getCargo()), ignored -> new ArrayList<>())
                .add(membro));
        return equipePorCargo;
    }

    private String getTituloGrupo(CargoEquipe cargo) {
        if (cargo == CargoEquipe.PRESIDENTE_CONSELHO || cargo == CargoEquipe.DIRETOR_EXECUTIVO) {
            return "Conselho de Administração";
        }
        return cargo.getDescricao();
    }

    private void validar(String nome, CargoEquipe cargo) {
        ValidationUtils.validarCampoStringObrigatorio(nome, "nome");
        if (cargo == null) {
            throw new IllegalArgumentException("O cargo é obrigatório.");
        }
    }
}
