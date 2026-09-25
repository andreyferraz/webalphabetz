package com.alphabetz.webalphabetz.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
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

    private final MembroEquipeRepository membroEquipeRepository;
    private final FileUploadService fileUploadService;
    private final CargoEquipeService cargoEquipeService;

    public EquipeService(MembroEquipeRepository membroEquipeRepository,
            FileUploadService fileUploadService,
            CargoEquipeService cargoEquipeService) {
        this.membroEquipeRepository = membroEquipeRepository;
        this.fileUploadService = fileUploadService;
        this.cargoEquipeService = cargoEquipeService;
    }

    @Transactional
    public MembroEquipe create(String nome, String cargo, MultipartFile imagem) {
        validar(nome, cargo);
        String cargoValidado = cargoEquipeService.requireExistingCargo(cargo);
        if (imagem == null || imagem.isEmpty()) {
            throw new IllegalArgumentException("A foto é obrigatória.");
        }

        String imagemUrl = fileUploadService.salvarImagem(imagem);
        try {
            MembroEquipe membro = new MembroEquipe();
            membro.setId(UUID.randomUUID());
            membro.setNome(nome.trim());
            membro.setCargo(cargoValidado);
            membro.setImagemUrl(imagemUrl);
            membro.setNew(true);
            return membroEquipeRepository.save(membro);
        } catch (RuntimeException exception) {
            fileUploadService.removerImagem(imagemUrl);
            throw exception;
        }
    }

    @Transactional
    public MembroEquipe update(UUID id, String nome, String cargo, MultipartFile imagem) {
        validar(nome, cargo);
        String cargoValidado = cargoEquipeService.requireExistingCargo(cargo);
        MembroEquipe membro = getById(id);
        membro.setNome(nome.trim());
        membro.setCargo(cargoValidado);

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
        Map<String, Integer> ordemPorCargo = cargoEquipeService.getAllCargos().stream()
                .collect(Collectors.toMap(
                        c -> c.getNome().toLowerCase(),
                        c -> c.getOrdem() != null ? c.getOrdem() : Integer.MAX_VALUE,
                        (existing, replacement) -> existing
                ));

        return StreamSupport.stream(membroEquipeRepository.findAll().spliterator(), false)
                .sorted(Comparator
                        .comparingInt((MembroEquipe membro) -> ordemPorCargo.getOrDefault(
                                membro.getCargo() != null ? membro.getCargo().toLowerCase() : "",
                                Integer.MAX_VALUE))
                        .thenComparing(MembroEquipe::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Map<String, List<MembroEquipe>> getGroupedByCargo() {
        Map<String, List<MembroEquipe>> equipePorCargo = new LinkedHashMap<>();
        getAll().forEach(membro -> equipePorCargo
                .computeIfAbsent(getTituloGrupo(membro.getCargo()), ignored -> new ArrayList<>())
                .add(membro));
        return equipePorCargo;
    }

    private String getTituloGrupo(String cargo) {
        if (cargo != null && ("Presidente do Conselho".equalsIgnoreCase(cargo) || "Diretor Executivo".equalsIgnoreCase(cargo))) {
            return "Conselho de Administração";
        }
        return cargo != null ? cargo : "";
    }

    private void validar(String nome, String cargo) {
        ValidationUtils.validarCampoStringObrigatorio(nome, "nome");
        ValidationUtils.validarCampoStringObrigatorio(cargo, "cargo");
    }
}
