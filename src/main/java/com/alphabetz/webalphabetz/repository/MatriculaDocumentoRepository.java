package com.alphabetz.webalphabetz.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.alphabetz.webalphabetz.model.MatriculaDocumento;

@Repository
public interface MatriculaDocumentoRepository extends CrudRepository<MatriculaDocumento, UUID> {

    Optional<MatriculaDocumento> findByTipo(String tipo);

    @Query("""
            SELECT id, tipo, nome_arquivo, tipo_conteudo, tamanho,
                   CAST(NULL AS BLOB) AS conteudo
            FROM matricula_documentos
            ORDER BY CASE tipo
                WHEN 'valor-anuidade' THEN 1
                WHEN 'contrato-educacional' THEN 2
                WHEN 'horario-personalizado' THEN 3
                ELSE 4
            END
            """)
    List<MatriculaDocumento> findAllMetadata();
}
