package com.alphabetz.webalphabetz.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("fundo_topo")
public class FundoTopo implements Persistable<UUID> {

    @Id
    @Column("id")
    private UUID id;

    @Column("nome_pagina")
    private String nomePagina;

    @Column("imagem_url")
    private String imagemUrl;

    @Transient
    private boolean isNew;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

}
