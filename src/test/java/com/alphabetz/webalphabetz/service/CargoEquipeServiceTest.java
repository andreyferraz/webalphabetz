package com.alphabetz.webalphabetz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alphabetz.webalphabetz.model.CargoEquipe;
import com.alphabetz.webalphabetz.repository.CargoEquipeRepository;

class CargoEquipeServiceTest {

    @Test
    @DisplayName("Deve criar um novo cargo incrementando a ordem")
    void deveCriarCargoComSucesso() {
        StubCargoEquipeRepository repository = new StubCargoEquipeRepository();
        repository.cargos.add(new CargoEquipe(UUID.randomUUID(), "Presidente do Conselho", 1, false));

        CargoEquipeService service = new CargoEquipeService(repository, null);
        CargoEquipe criado = service.createCargo("  Coordenador de TI  ");

        assertThat(criado.getNome()).isEqualTo("Coordenador de TI");
        assertThat(criado.getOrdem()).isEqualTo(2);
        assertThat(repository.cargos).hasSize(2);
    }

    @Test
    @DisplayName("Deve impedir criação de cargo duplicado (case insensitive)")
    void deveImpedirCriacaoDeCargoDuplicado() {
        StubCargoEquipeRepository repository = new StubCargoEquipeRepository();
        repository.cargos.add(new CargoEquipe(UUID.randomUUID(), "Diretor Executivo", 1, false));

        CargoEquipeService service = new CargoEquipeService(repository, null);

        assertThatThrownBy(() -> service.createCargo("diretor executivo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está cadastrado");
    }

    @Test
    @DisplayName("Deve validar cargo obrigatório existente")
    void deveValidarCargoExistente() {
        StubCargoEquipeRepository repository = new StubCargoEquipeRepository();
        repository.cargos.add(new CargoEquipe(UUID.randomUUID(), "Berçarista", 1, false));

        CargoEquipeService service = new CargoEquipeService(repository, null);

        assertThat(service.requireExistingCargo("berçarista")).isEqualTo("Berçarista");

        assertThatThrownBy(() -> service.requireExistingCargo("Cargo Inexistente"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Selecione um cargo cadastrado");
    }

    private static class StubCargoEquipeRepository implements CargoEquipeRepository {
        final List<CargoEquipe> cargos = new ArrayList<>();

        @Override
        public List<CargoEquipe> findAllByOrderByOrdemAscNomeAsc() {
            return new ArrayList<>(cargos);
        }

        @Override
        public <S extends CargoEquipe> S save(S entity) {
            cargos.removeIf(c -> c.getId().equals(entity.getId()));
            cargos.add(entity);
            return entity;
        }

        @Override
        public <S extends CargoEquipe> Iterable<S> saveAll(Iterable<S> entities) {
            return entities;
        }

        @Override
        public Optional<CargoEquipe> findById(UUID id) {
            return cargos.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public boolean existsById(UUID id) {
            return cargos.stream().anyMatch(c -> c.getId().equals(id));
        }

        @Override
        public Iterable<CargoEquipe> findAll() {
            return cargos;
        }

        @Override
        public Iterable<CargoEquipe> findAllById(Iterable<UUID> ids) {
            return Collections.emptyList();
        }

        @Override
        public long count() {
            return cargos.size();
        }

        @Override
        public void deleteById(UUID id) {
            cargos.removeIf(c -> c.getId().equals(id));
        }

        @Override
        public void delete(CargoEquipe entity) {
            cargos.remove(entity);
        }

        @Override
        public void deleteAllById(Iterable<? extends UUID> ids) {
        }

        @Override
        public void deleteAll(Iterable<? extends CargoEquipe> entities) {
        }

        @Override
        public void deleteAll() {
            cargos.clear();
        }
    }
}
