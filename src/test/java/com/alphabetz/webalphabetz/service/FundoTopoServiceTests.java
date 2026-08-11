package com.alphabetz.webalphabetz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.model.FundoTopo;
import com.alphabetz.webalphabetz.repository.FundoTopoRepository;

class FundoTopoServiceTests {

    @Test
    void createFundoTopoPersistsRecordWithUploadedImage() {
        RecordingFundoTopoRepository repository = new RecordingFundoTopoRepository();
        RecordingFileUploadService fileUploadService = new RecordingFileUploadService();
        FundoTopoService service = new FundoTopoService(repository, fileUploadService);

        FundoTopo saved = service.createFundoTopo("  Página inicial  ", imageFile("fundo.jpg"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.isNew()).isTrue();
        assertThat(saved.getNomePagina()).isEqualTo("Página inicial");
        assertThat(saved.getImagemUrl()).isEqualTo("fundo.webp");
        assertThat(repository.savedEntities).containsExactly(saved);
        assertThat(fileUploadService.savedFiles).containsExactly("fundo.jpg");
        assertThat(fileUploadService.removedFiles).isEmpty();
    }

    @Test
    void createFundoTopoRejectsBlankPageNameBeforeUploadingImage() {
        RecordingFundoTopoRepository repository = new RecordingFundoTopoRepository();
        RecordingFileUploadService fileUploadService = new RecordingFileUploadService();
        FundoTopoService service = new FundoTopoService(repository, fileUploadService);

        assertThatThrownBy(() -> service.createFundoTopo("   ", imageFile("fundo.jpg")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("nomePagina é obrigatório.");
        assertThat(repository.savedEntities).isEmpty();
        assertThat(fileUploadService.savedFiles).isEmpty();
    }

    @Test
    void createFundoTopoRequiresImage() {
        RecordingFundoTopoRepository repository = new RecordingFundoTopoRepository();
        RecordingFileUploadService fileUploadService = new RecordingFileUploadService();
        FundoTopoService service = new FundoTopoService(repository, fileUploadService);

        assertThatThrownBy(() -> service.createFundoTopo("Página inicial", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A imagem é obrigatória.");
        assertThat(repository.savedEntities).isEmpty();
    }

    @Test
    void createFundoTopoRemovesImageWhenSaveFails() {
        RecordingFundoTopoRepository repository = new RecordingFundoTopoRepository();
        repository.failOnSave = true;
        RecordingFileUploadService fileUploadService = new RecordingFileUploadService();
        FundoTopoService service = new FundoTopoService(repository, fileUploadService);

        assertThatThrownBy(() -> service.createFundoTopo("Página inicial", imageFile("fundo.jpg")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("falha ao salvar");
        assertThat(fileUploadService.removedFiles).containsExactly("fundo.webp");
    }

    @Test
    void updateFundoTopoChangesNameAndKeepsCurrentImageWhenNoImageIsSent() {
        RecordingFundoTopoRepository repository = new RecordingFundoTopoRepository();
        FundoTopo existing = fundo("Página inicial", "antigo.webp");
        repository.addExisting(existing);
        RecordingFileUploadService fileUploadService = new RecordingFileUploadService();
        FundoTopoService service = new FundoTopoService(repository, fileUploadService);

        FundoTopo updated = service.updateFundoTopo(existing.getId(), "  Escola  ", null);

        assertThat(updated.getNomePagina()).isEqualTo("Escola");
        assertThat(updated.getImagemUrl()).isEqualTo("antigo.webp");
        assertThat(fileUploadService.savedFiles).isEmpty();
        assertThat(fileUploadService.removedFiles).isEmpty();
    }

    @Test
    void updateFundoTopoReplacesImageAndRemovesPreviousFile() {
        RecordingFundoTopoRepository repository = new RecordingFundoTopoRepository();
        FundoTopo existing = fundo("Página inicial", "antigo.webp");
        repository.addExisting(existing);
        RecordingFileUploadService fileUploadService = new RecordingFileUploadService();
        FundoTopoService service = new FundoTopoService(repository, fileUploadService);

        FundoTopo updated = service.updateFundoTopo(
                existing.getId(), "Página inicial", imageFile("novo.jpg"));

        assertThat(updated.getImagemUrl()).isEqualTo("fundo.webp");
        assertThat(fileUploadService.savedFiles).containsExactly("novo.jpg");
        assertThat(fileUploadService.removedFiles).containsExactly("antigo.webp");
    }

    @Test
    void updateFundoTopoRemovesNewImageWhenSaveFails() {
        RecordingFundoTopoRepository repository = new RecordingFundoTopoRepository();
        FundoTopo existing = fundo("Página inicial", "antigo.webp");
        repository.addExisting(existing);
        repository.failOnSave = true;
        RecordingFileUploadService fileUploadService = new RecordingFileUploadService();
        FundoTopoService service = new FundoTopoService(repository, fileUploadService);

        assertThatThrownBy(() -> service.updateFundoTopo(
                existing.getId(), "Página inicial", imageFile("novo.jpg")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("falha ao salvar");
        assertThat(fileUploadService.removedFiles).containsExactly("fundo.webp");
    }

    @Test
    void deleteFundoTopoDeletesRecordAndAssociatedImage() {
        RecordingFundoTopoRepository repository = new RecordingFundoTopoRepository();
        FundoTopo existing = fundo("Página inicial", "antigo.webp");
        repository.addExisting(existing);
        RecordingFileUploadService fileUploadService = new RecordingFileUploadService();
        FundoTopoService service = new FundoTopoService(repository, fileUploadService);

        service.deleteFundoTopo(existing.getId());

        assertThat(repository.findById(existing.getId())).isEmpty();
        assertThat(fileUploadService.removedFiles).containsExactly("antigo.webp");
    }

    @Test
    void getByIdThrowsWhenRecordDoesNotExist() {
        FundoTopoService service = new FundoTopoService(
                new RecordingFundoTopoRepository(), new RecordingFileUploadService());
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Fundo não encontrado.");
    }

    @Test
    void getAllReturnsRecordsOrderedByPageName() {
        RecordingFundoTopoRepository repository = new RecordingFundoTopoRepository();
        repository.addExisting(fundo("Turmas", "turmas.webp"));
        repository.addExisting(fundo("Abordagem", "abordagem.webp"));
        repository.addExisting(fundo("Página inicial", "inicio.webp"));
        FundoTopoService service = new FundoTopoService(repository, new RecordingFileUploadService());

        assertThat(service.getAll())
                .extracting(FundoTopo::getNomePagina)
                .containsExactly("Abordagem", "Página inicial", "Turmas");
    }

    private static MockMultipartFile imageFile(String filename) {
        return new MockMultipartFile(
                "imagem", filename, "image/jpeg", new byte[] { 1, 2, 3 });
    }

    private static FundoTopo fundo(String nomePagina, String imagemUrl) {
        FundoTopo fundo = new FundoTopo();
        fundo.setId(UUID.randomUUID());
        fundo.setNomePagina(nomePagina);
        fundo.setImagemUrl(imagemUrl);
        return fundo;
    }

    private static final class RecordingFileUploadService extends FileUploadService {

        private final List<String> savedFiles = new ArrayList<>();
        private final List<String> removedFiles = new ArrayList<>();

        @Override
        public String salvarImagem(MultipartFile imagemFile) {
            savedFiles.add(imagemFile.getOriginalFilename());
            return "fundo.webp";
        }

        @Override
        public void removerImagem(String nomeArquivo) {
            removedFiles.add(nomeArquivo);
        }
    }

    private static final class RecordingFundoTopoRepository implements FundoTopoRepository {

        private final Map<UUID, FundoTopo> storage = new HashMap<>();
        private final List<FundoTopo> savedEntities = new ArrayList<>();
        private boolean failOnSave;

        private void addExisting(FundoTopo entity) {
            storage.put(entity.getId(), entity);
        }

        @Override
        public <S extends FundoTopo> S save(S entity) {
            if (failOnSave) {
                throw new IllegalStateException("falha ao salvar");
            }
            storage.put(entity.getId(), entity);
            savedEntities.add(entity);
            return entity;
        }

        @Override
        public <S extends FundoTopo> Iterable<S> saveAll(Iterable<S> entities) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<FundoTopo> findById(UUID id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public boolean existsById(UUID id) {
            return storage.containsKey(id);
        }

        @Override
        public Iterable<FundoTopo> findAll() {
            return storage.values();
        }

        @Override
        public List<FundoTopo> findAllByOrderByNomeAsc() {
            return storage.values().stream()
                    .sorted(Comparator.comparing(FundoTopo::getNomePagina))
                    .toList();
        }

        @Override
        public Iterable<FundoTopo> findAllById(Iterable<UUID> ids) {
            List<FundoTopo> items = new ArrayList<>();
            for (UUID id : ids) {
                FundoTopo item = storage.get(id);
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
        public void delete(FundoTopo entity) {
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
        public void deleteAll(Iterable<? extends FundoTopo> entities) {
            for (FundoTopo entity : entities) {
                delete(entity);
            }
        }

        @Override
        public void deleteAll() {
            storage.clear();
        }
    }
}
