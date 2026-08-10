package com.alphabetz.webalphabetz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.alphabetz.webalphabetz.model.Depoimentos;
import com.alphabetz.webalphabetz.repository.DepoimentosRepository;

class DepoimentosServiceTests {

    @Test
    void createDepoimentoPersistsTestimonialWithGeneratedImage() {
        RecordingDepoimentosRepository repository = new RecordingDepoimentosRepository();
        RecordingFileUploadService fileUploadService = new RecordingFileUploadService();
        DepoimentosService service = new DepoimentosService(repository, fileUploadService);

        Depoimentos request = new Depoimentos();
        request.setNome("Maria Silva");
        request.setDepoimento("Excelente experiência.");
        MockMultipartFile imageFile = new MockMultipartFile(
                "imagem", "foto.jpg", "image/jpeg", new byte[] { 1, 2, 3 });

        Depoimentos saved = service.createDepoimento(request, imageFile);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.isNew()).isTrue();
        assertThat(saved.getImagemUrl()).isEqualTo("depoimento.webp");
        assertThat(repository.savedEntities).hasSize(1);
        assertThat(repository.savedEntities.get(0).getNome()).isEqualTo("Maria Silva");
        assertThat(fileUploadService.savedFiles).containsExactly("foto.jpg");
    }

    @Test
    void createDepoimentoRemovesImageWhenSaveFails() {
        RecordingDepoimentosRepository repository = new RecordingDepoimentosRepository();
        repository.failOnSave = true;
        RecordingFileUploadService fileUploadService = new RecordingFileUploadService();
        DepoimentosService service = new DepoimentosService(repository, fileUploadService);

        Depoimentos request = new Depoimentos();
        request.setNome("João Pereira");
        request.setDepoimento("Muito bom.");
        MockMultipartFile imageFile = new MockMultipartFile(
                "imagem", "foto.jpg", "image/jpeg", new byte[] { 1, 2, 3 });

        assertThatThrownBy(() -> service.createDepoimento(request, imageFile))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("falha ao salvar");
        assertThat(fileUploadService.removedFiles).containsExactly("depoimento.webp");
    }

    private static final class RecordingFileUploadService extends FileUploadService {

        private final List<String> savedFiles = new ArrayList<>();
        private final List<String> removedFiles = new ArrayList<>();

        @Override
        public String salvarImagem(org.springframework.web.multipart.MultipartFile imagemFile) {
            savedFiles.add(imagemFile.getOriginalFilename());
            return "depoimento.webp";
        }

        @Override
        public void removerImagem(String nomeArquivo) {
            removedFiles.add(nomeArquivo);
        }
    }

    private static final class RecordingDepoimentosRepository implements DepoimentosRepository {

        private final Map<UUID, Depoimentos> storage = new HashMap<>();
        private final List<Depoimentos> savedEntities = new ArrayList<>();
        private boolean failOnSave;

        @Override
        public <S extends Depoimentos> S save(S entity) {
            if (failOnSave) {
                throw new IllegalStateException("falha ao salvar");
            }
            storage.put(entity.getId(), entity);
            savedEntities.add(entity);
            return entity;
        }

        @Override
        public <S extends Depoimentos> Iterable<S> saveAll(Iterable<S> entities) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Depoimentos> findById(UUID id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public boolean existsById(UUID id) {
            return storage.containsKey(id);
        }

        @Override
        public Iterable<Depoimentos> findAll() {
            return storage.values();
        }

        @Override
        public Iterable<Depoimentos> findAllById(Iterable<UUID> ids) {
            List<Depoimentos> items = new ArrayList<>();
            for (UUID id : ids) {
                Depoimentos item = storage.get(id);
                if (item != null) {
                    items.add(item);
                }
            }
            return items;
        }

        @Override
        public long count() {
            return storage.size();
        }

        @Override
        public void deleteById(UUID id) {
            storage.remove(id);
        }

        @Override
        public void delete(Depoimentos entity) {
            if (entity != null && entity.getId() != null) {
                storage.remove(entity.getId());
            }
        }

        @Override
        public void deleteAllById(Iterable<? extends UUID> ids) {
            for (UUID id : ids) {
                storage.remove(id);
            }
        }

        @Override
        public void deleteAll(Iterable<? extends Depoimentos> entities) {
            for (Depoimentos entity : entities) {
                delete(entity);
            }
        }

        @Override
        public void deleteAll() {
            storage.clear();
        }
    }
}