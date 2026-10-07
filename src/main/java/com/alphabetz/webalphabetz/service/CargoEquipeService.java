package com.alphabetz.webalphabetz.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public void reorderCargos(List<UUID> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new IllegalArgumentException("A lista de cargos não pode ser vazia.");
        }

        List<CargoEquipe> allCargos = getAllCargos();
        Map<UUID, CargoEquipe> cargoMap = allCargos.stream()
                .collect(Collectors.toMap(CargoEquipe::getId, c -> c));

        List<CargoEquipe> reordered = new ArrayList<>();
        int currentOrder = 1;

        for (UUID id : orderedIds) {
            CargoEquipe cargo = cargoMap.remove(id);
            if (cargo != null) {
                cargo.setOrdem(currentOrder++);
                cargo.setNew(false);
                reordered.add(cargo);
            }
        }

        for (CargoEquipe remaining : cargoMap.values()) {
            remaining.setOrdem(currentOrder++);
            remaining.setNew(false);
            reordered.add(remaining);
        }

        cargoEquipeRepository.saveAll(reordered);
    }

    @Transactional
    public void moveCargo(UUID id, String direction) {
        if (id == null) {
            throw new IllegalArgumentException("ID do cargo não pode ser nulo.");
        }
        if (direction == null || (!direction.equalsIgnoreCase("cima")
                && !direction.equalsIgnoreCase("up")
                && !direction.equalsIgnoreCase("baixo")
                && !direction.equalsIgnoreCase("down"))) {
            throw new IllegalArgumentException("Direção inválida. Use 'cima' ou 'baixo'.");
        }

        List<CargoEquipe> allCargos = new ArrayList<>(getAllCargos());
        int currentIndex = -1;
        for (int i = 0; i < allCargos.size(); i++) {
            if (allCargos.get(i).getId().equals(id)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) {
            throw new IllegalArgumentException("Cargo não encontrado.");
        }

        boolean isUp = direction.equalsIgnoreCase("cima") || direction.equalsIgnoreCase("up");
        if (isUp && currentIndex > 0) {
            Collections.swap(allCargos, currentIndex, currentIndex - 1);
        } else if (!isUp && currentIndex < allCargos.size() - 1) {
            Collections.swap(allCargos, currentIndex, currentIndex + 1);
        }

        for (int i = 0; i < allCargos.size(); i++) {
            CargoEquipe c = allCargos.get(i);
            c.setOrdem(i + 1);
            c.setNew(false);
        }

        cargoEquipeRepository.saveAll(allCargos);
    }

    @Transactional
    public void moveCargoUp(UUID id) {
        moveCargo(id, "cima");
    }

    @Transactional
    public void moveCargoDown(UUID id) {
        moveCargo(id, "baixo");
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
