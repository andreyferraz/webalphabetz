package com.alphabetz.webalphabetz.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.alphabetz.webalphabetz.model.DocumentoInstitucional;

@Repository
public interface DocumentoInstitucionalRepository extends CrudRepository<DocumentoInstitucional, UUID> {

    @Query("""
            SELECT id, nome, nome_arquivo, tipo_conteudo, tamanho
            FROM documentos_institucionais
            ORDER BY nome COLLATE NOCASE
            """)
    List<DocumentoInstitucional> findAllMetadata();
}
