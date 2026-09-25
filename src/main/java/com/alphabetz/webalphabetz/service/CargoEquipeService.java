package com.alphabetz.webalphabetz.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alphabetz.webalphabetz.model.CargoEquipe;
import com.alphabetz.webalphabetz.repository.CargoEquipeRepository;
import com.alphabetz.webalphabetz.utils.ValidationUtils;

@Service
public class CargoEquipeService {

    private static final int MAX_CARGO_LENGTH = 80;

    private final CargoEquipeRepository cargoEquipeRepository;
    private final JdbcTemplate jdbcTemplate;

    public CargoEquipeService(CargoEquipeRepository cargoEquipeRepository,
            JdbcTemplate jdbcTemplate) {
        this.cargoEquipeRepository = cargoEquipeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CargoEquipe> getAllCargos() {
        return cargoEquipeRepository.findAllByOrderByOrdemAscNomeAsc();
    }

    @Transactional
    public CargoEquipe createCargo(String nome) {
        String normalizedName = normalizeName(nome);
        if (findByName(normalizedName) != null) {
            throw new IllegalArgumentException("Esse cargo já está cadastrado.");
        }

        int nextOrdem = getAllCargos().stream()
                .mapToInt(c -> c.getOrdem() != null ? c.getOrdem() : 0)
                .max()
                .orElse(0) + 1;

        CargoEquipe cargo = new CargoEquipe();
        cargo.setId(UUID.randomUUID());
        cargo.setNome(normalizedName);
        cargo.setOrdem(nextOrdem);
        cargo.setNew(true);

        try {
            return cargoEquipeRepository.save(cargo);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("Esse cargo já está cadastrado.", exception);
        }
    }

    @Transactional
    public CargoEquipe updateCargo(UUID id, String nome) {
        CargoEquipe cargo = findById(id);
        String normalizedName = normalizeName(nome);
        CargoEquipe cargoWithSameName = findByName(normalizedName);
        if (cargoWithSameName != null && !cargoWithSameName.getId().equals(cargo.getId())) {
            throw new IllegalArgumentException("Esse cargo já está cadastrado.");
        }

        String oldName = cargo.getNome();
        cargo.setNome(normalizedName);
        cargo.setNew(false);

        try {
            CargoEquipe updatedCargo = cargoEquipeRepository.save(cargo);
            jdbcTemplate.update(
                    "UPDATE equipe_membros SET cargo = ? WHERE cargo = ? COLLATE NOCASE",
                    normalizedName, oldName);
            return updatedCargo;
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("Esse cargo já está cadastrado.", exception);
        }
    }

    @Transactional
    public void deleteCargo(UUID id) {
        CargoEquipe cargo = findById(id);
        Long membersUsingCargo = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM equipe_membros WHERE cargo = ? COLLATE NOCASE",
                Long.class, cargo.getNome());
        if (membersUsingCargo != null && membersUsingCargo > 0) {
            throw new IllegalArgumentException(
                    "Esse cargo está vinculado a profissionais da equipe e não pode ser removido.");
        }
        cargoEquipeRepository.deleteById(cargo.getId());
    }

    public String requireExistingCargo(String nome) {
        String normalizedName = normalizeName(nome);
        CargoEquipe cargo = findByName(normalizedName);
        if (cargo == null) {
            throw new IllegalArgumentException("Selecione um cargo cadastrado.");
        }
        return cargo.getNome();
    }

    public CargoEquipe findByName(String nome) {
        return getAllCargos().stream()
                .filter(cargo -> cargo.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }

    public CargoEquipe findById(UUID id) {
        return cargoEquipeRepository.findById(Objects.requireNonNull(id, "id"))
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado."));
    }

    private String normalizeName(String nome) {
        ValidationUtils.validarCampoStringObrigatorio(nome, "cargo");
        String normalizedName = nome.trim().replaceAll("\\s+", " ");
        if (normalizedName.length() > MAX_CARGO_LENGTH) {
            throw new IllegalArgumentException("O cargo deve ter no máximo 80 caracteres.");
        }
        return normalizedName;
    }
}
