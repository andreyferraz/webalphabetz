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
import com.alphabetz.webalphabetz.repository.MembroEquipeRepository;

class EquipeServiceTest {

    @Test
    @DisplayName("Deve possuir a descrição correta para Diretor Executivo sem CEO")
    void devePossuirDescricaoCorretaParaDiretorExecutivo() {
        assertThat(CargoEquipe.DIRETOR_EXECUTIVO.getDescricao()).isEqualTo("Diretor Executivo");
    }

    @Test
    @DisplayName("Deve agrupar Diretor Executivo no Conselho de Administração")
    void deveAgruparDiretorExecutivoNoConselhoDeAdministracao() {
        StubMembroEquipeRepository repository = new StubMembroEquipeRepository();
        MembroEquipe membro = new MembroEquipe(UUID.randomUUID(), "Rafael Martins", CargoEquipe.DIRETOR_EXECUTIVO, "foto.webp", false);
        repository.membros.add(membro);

        EquipeService service = new EquipeService(repository, null);
        Map<String, List<MembroEquipe>> agrupado = service.getGroupedByCargo();

        assertThat(agrupado).containsKey("Conselho de Administração");
        assertThat(agrupado.get("Conselho de Administração")).containsExactly(membro);
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

