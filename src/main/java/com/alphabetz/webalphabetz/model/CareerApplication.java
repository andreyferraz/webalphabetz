package com.alphabetz.webalphabetz.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@Table("career_applications")
public class CareerApplication implements Persistable<UUID> {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    @Id
    @Column("id")
    private UUID id;

    @Column("nome")
    private String nome;

    @Column("email")
    private String email;

    @Column("telefone")
    private String telefone;

    @Column("area")
    private String area;

    @Column("formacao")
    private String formacao;

    @Column("disponibilidade")
    private String disponibilidade;

    @Column("experiencia")
    private String experiencia;

    @Column("linkedin")
    private String linkedin;

    @Column("consentimento")
    private int consentimento;

    @Column("enviado_em")
    private LocalDateTime enviadoEm;

    @Column("curriculo_nome")
    private String curriculoNome;

    @Column("curriculo_tipo")
    private String curriculoTipo;

    @Column("curriculo_tamanho")
    private long curriculoTamanho;

    @Column("curriculo_conteudo")
    private byte[] curriculoConteudo;

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

    public boolean isConsentimento() {
        return consentimento == 1;
    }

    public void setConsentimento(boolean consentimento) {
        this.consentimento = consentimento ? 1 : 0;
    }

    @Transient
    public String getEnviadoEmFormatado() {
        return enviadoEm == null ? "" : enviadoEm.format(DISPLAY_DATE_FORMAT);
    }
}
