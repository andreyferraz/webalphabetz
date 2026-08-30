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
@Table("matricula_documentos")
public class MatriculaDocumento implements Persistable<UUID> {

    @Id
    @Column("id")
    private UUID id;

    @Column("tipo")
    private String tipo;

    @Column("nome_arquivo")
    private String nomeArquivo;

    @Column("tipo_conteudo")
    private String tipoConteudo;

    @Column("tamanho")
    private long tamanho;

    @Column("conteudo")
    private byte[] conteudo;

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
