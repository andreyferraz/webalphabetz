package com.alphabetz.webalphabetz.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.alphabetz.webalphabetz.model.CargoEquipe;
import com.alphabetz.webalphabetz.model.MembroEquipe;
import com.alphabetz.webalphabetz.repository.CargoEquipeRepository;
import com.alphabetz.webalphabetz.repository.MembroEquipeRepository;

class EquipeServiceTest {

    @Test
    @DisplayName("Deve possuir a descrição correta para CargoEquipe")
    void devePossuirDescricaoCorretaParaCargo() {
        CargoEquipe cargo = new CargoEquipe(UUID.randomUUID(), "Diretor Executivo", 2, false);
        assertThat(cargo.getDescricao()).isEqualTo("Diretor Executivo");
    }

    @Test
    @DisplayName("Deve agrupar Diretor Executivo e Presidente do Conselho no Conselho de Administração")
    void deveAgruparDiretorExecutivoNoConselhoDeAdministracao() {
        StubMembroEquipeRepository repository = new StubMembroEquipeRepository();
        StubCargoEquipeRepository cargoRepository = new StubCargoEquipeRepository();
        cargoRepository.cargos.add(new CargoEquipe(UUID.randomUUID(), "Diretor Executivo", 2, false));
        CargoEquipeService cargoEquipeService = new CargoEquipeService(cargoRepository, null);

        MembroEquipe membro = new MembroEquipe(UUID.randomUUID(), "Rafael Martins", "Diretor Executivo", "foto.webp", false);
        repository.membros.add(membro);

        EquipeService service = new EquipeService(repository, null, cargoEquipeService);
        Map<String, List<MembroEquipe>> agrupado = service.getGroupedByCargo();

        assertThat(agrupado).containsKey("Conselho de Administração");
        assertThat(agrupado.get("Conselho de Administração")).containsExactly(membro);
    }

    @Test
    @DisplayName("Deve ordenar membros respeitando a hierarquia da ordem dos cargos")
    void deveOrdenarMembrosRespeitandoHierarquiaDosCargos() {
        StubMembroEquipeRepository repository = new StubMembroEquipeRepository();
        StubCargoEquipeRepository cargoRepository = new StubCargoEquipeRepository();
        cargoRepository.cargos.add(new CargoEquipe(UUID.randomUUID(), "Presidente do Conselho", 1, false));
        cargoRepository.cargos.add(new CargoEquipe(UUID.randomUUID(), "Diretor Executivo", 2, false));
        cargoRepository.cargos.add(new CargoEquipe(UUID.randomUUID(), "Professora Regente", 6, false));
        CargoEquipeService cargoEquipeService = new CargoEquipeService(cargoRepository, null);

        MembroEquipe m1 = new MembroEquipe(UUID.randomUUID(), "Camila Rocha", "Professora Regente", "foto1.webp", false);
        MembroEquipe m2 = new MembroEquipe(UUID.randomUUID(), "Rafael Martins", "Diretor Executivo", "foto2.webp", false);
        MembroEquipe m3 = new MembroEquipe(UUID.randomUUID(), "Ana Clara Mendes", "Presidente do Conselho", "foto3.webp", false);
        repository.membros.addAll(List.of(m1, m2, m3));

        EquipeService service = new EquipeService(repository, null, cargoEquipeService);
        List<MembroEquipe> ordenados = service.getAll();

        assertThat(ordenados).containsExactly(m3, m2, m1);
    }

    private static class StubCargoEquipeRepository implements CargoEquipeRepository {
        final List<CargoEquipe> cargos = new ArrayList<>();

        @Override
        public List<CargoEquipe> findAllByOrderByOrdemAscNomeAsc() {
            return new ArrayList<>(cargos);
        }

        @Override
        public <S extends CargoEquipe> S save(S entity) {
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

    private static class StubMembroEquipeRepository implements MembroEquipeRepository {
        final List<MembroEquipe> membros = new ArrayList<>();

        @Override
        public Iterable<MembroEquipe> findAll() {
            return membros;
        }

        @Override
        public <S extends MembroEquipe> S save(S entity) {
            return entity;
        }

        @Override
        public <S extends MembroEquipe> Iterable<S> saveAll(Iterable<S> entities) {
            return entities;
        }

        @Override
        public Optional<MembroEquipe> findById(UUID id) {
            return membros.stream().filter(m -> m.getId().equals(id)).findFirst();
        }

        @Override
        public boolean existsById(UUID id) {
            return membros.stream().anyMatch(m -> m.getId().equals(id));
        }

        @Override
        public Iterable<MembroEquipe> findAllById(Iterable<UUID> ids) {
            return Collections.emptyList();
        }

        @Override
        public long count() {
            return membros.size();
        }

        @Override
        public void deleteById(UUID id) {
            membros.removeIf(m -> m.getId().equals(id));
        }

        @Override
        public void delete(MembroEquipe entity) {
            membros.remove(entity);
        }

        @Override
        public void deleteAllById(Iterable<? extends UUID> ids) {
        }

        @Override
        public void deleteAll(Iterable<? extends MembroEquipe> entities) {
        }

        @Override
        public void deleteAll() {
            membros.clear();
        }
    }
}
