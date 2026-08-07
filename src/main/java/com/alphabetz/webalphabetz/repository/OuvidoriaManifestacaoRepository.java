package com.alphabetz.webalphabetz.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.alphabetz.webalphabetz.model.OuvidoriaManifestacao;

@Repository
public interface OuvidoriaManifestacaoRepository extends CrudRepository<OuvidoriaManifestacao, UUID> {

    @Query("""
            SELECT id, nome, sobrenome, email, celular, perfil, area_atuacao, assunto,
                   detalhes, receber_retorno, enviado_em, arquivo_nome, arquivo_tipo,
                   arquivo_tamanho, CAST(NULL AS BLOB) AS arquivo_conteudo,
                   documento_imagem_nome, documento_imagem_tipo, documento_imagem_tamanho,
                   CAST(NULL AS BLOB) AS documento_imagem_conteudo
            FROM ouvidoria_manifestacoes
            ORDER BY enviado_em DESC
            """)
    List<OuvidoriaManifestacao> findAllNewestFirst();
}
