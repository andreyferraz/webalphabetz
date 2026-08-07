package com.alphabetz.webalphabetz.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.alphabetz.webalphabetz.model.CareerApplication;

@Repository
public interface CareerApplicationRepository extends CrudRepository<CareerApplication, UUID> {

    @Query("""
            SELECT id, nome, email, telefone, area, formacao, disponibilidade,
                   experiencia, linkedin, consentimento, enviado_em,
                   curriculo_nome, curriculo_tipo, curriculo_tamanho,
                   CAST(NULL AS BLOB) AS curriculo_conteudo
            FROM career_applications
            ORDER BY enviado_em DESC
            """)
    List<CareerApplication> findAllNewestFirst();
}
