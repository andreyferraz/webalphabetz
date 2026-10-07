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

    @Test
    @DisplayName("Deve reordenar cargos com base na lista de IDs")
    void deveReordenarCargosComSucesso() {
        StubCargoEquipeRepository repository = new StubCargoEquipeRepository();
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();
        repository.cargos.add(new CargoEquipe(id1, "Presidente", 1, false));
        repository.cargos.add(new CargoEquipe(id2, "Diretor", 2, false));
        repository.cargos.add(new CargoEquipe(id3, "Professor", 3, false));

        CargoEquipeService service = new CargoEquipeService(repository, null);
        service.reorderCargos(List.of(id3, id1, id2));

        List<CargoEquipe> ordenados = service.getAllCargos();
        assertThat(ordenados).extracting(CargoEquipe::getNome)
                .containsExactly("Professor", "Presidente", "Diretor");
        assertThat(ordenados.get(0).getOrdem()).isEqualTo(1);
        assertThat(ordenados.get(1).getOrdem()).isEqualTo(2);
        assertThat(ordenados.get(2).getOrdem()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve mover cargo para cima")
    void deveMoverCargoParaCima() {
        StubCargoEquipeRepository repository = new StubCargoEquipeRepository();
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        repository.cargos.add(new CargoEquipe(id1, "Presidente", 1, false));
        repository.cargos.add(new CargoEquipe(id2, "Diretor", 2, false));

        CargoEquipeService service = new CargoEquipeService(repository, null);
        service.moveCargo(id2, "cima");

        List<CargoEquipe> ordenados = service.getAllCargos();
        assertThat(ordenados).extracting(CargoEquipe::getNome)
                .containsExactly("Diretor", "Presidente");
    }

    @Test
    @DisplayName("Deve mover cargo para baixo")
    void deveMoverCargoParaBaixo() {
        StubCargoEquipeRepository repository = new StubCargoEquipeRepository();
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        repository.cargos.add(new CargoEquipe(id1, "Presidente", 1, false));
        repository.cargos.add(new CargoEquipe(id2, "Diretor", 2, false));

        CargoEquipeService service = new CargoEquipeService(repository, null);
        service.moveCargo(id1, "baixo");

        List<CargoEquipe> ordenados = service.getAllCargos();
        assertThat(ordenados).extracting(CargoEquipe::getNome)
                .containsExactly("Diretor", "Presidente");
    }

    @Test
    @DisplayName("Deve manter ordem ao tentar mover primeiro cargo para cima")
    void deveManterOrdemAoMoverPrimeiroParaCima() {
        StubCargoEquipeRepository repository = new StubCargoEquipeRepository();
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        repository.cargos.add(new CargoEquipe(id1, "Presidente", 1, false));
        repository.cargos.add(new CargoEquipe(id2, "Diretor", 2, false));

        CargoEquipeService service = new CargoEquipeService(repository, null);
        service.moveCargo(id1, "cima");

        List<CargoEquipe> ordenados = service.getAllCargos();
        assertThat(ordenados).extracting(CargoEquipe::getNome)
                .containsExactly("Presidente", "Diretor");
    }

    private static class StubCargoEquipeRepository implements CargoEquipeRepository {
        final List<CargoEquipe> cargos = new ArrayList<>();

        @Override
        public List<CargoEquipe> findAllByOrderByOrdemAscNomeAsc() {
            return cargos.stream()
                    .sorted((c1, c2) -> {
                        int ord1 = c1.getOrdem() != null ? c1.getOrdem() : 0;
                        int ord2 = c2.getOrdem() != null ? c2.getOrdem() : 0;
                        if (ord1 != ord2) {
                            return Integer.compare(ord1, ord2);
                        }
                        return c1.getNome().compareToIgnoreCase(c2.getNome());
                    })
                    .toList();
        }

        @Override
        public <S extends CargoEquipe> S save(S entity) {
            cargos.removeIf(c -> c.getId().equals(entity.getId()));
            cargos.add(entity);
            return entity;
        }

        @Override
        public <S extends CargoEquipe> Iterable<S> saveAll(Iterable<S> entities) {
            for (S entity : entities) {
                save(entity);
            }
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
