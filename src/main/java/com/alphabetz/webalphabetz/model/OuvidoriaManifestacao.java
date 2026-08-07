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
@Table("ouvidoria_manifestacoes")
public class OuvidoriaManifestacao implements Persistable<UUID> {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    @Id
    private UUID id;
    private String nome;
    private String sobrenome;
    private String email;
    private String celular;
    private String perfil;

    @Column("area_atuacao")
    private String areaAtuacao;

    private String assunto;
    private String detalhes;

    @Column("receber_retorno")
    private int receberRetorno;

    @Column("enviado_em")
    private LocalDateTime enviadoEm;

    @Column("arquivo_nome")
    private String arquivoNome;

    @Column("arquivo_tipo")
    private String arquivoTipo;

    @Column("arquivo_tamanho")
    private long arquivoTamanho;

    @Column("arquivo_conteudo")
    private byte[] arquivoConteudo;

    @Column("documento_imagem_nome")
    private String documentoImagemNome;

    @Column("documento_imagem_tipo")
    private String documentoImagemTipo;

    @Column("documento_imagem_tamanho")
    private long documentoImagemTamanho;

    @Column("documento_imagem_conteudo")
    private byte[] documentoImagemConteudo;

    @Transient
    private boolean isNew;

    @Override
    public boolean isNew() {
        return isNew;
    }

    public boolean isReceberRetorno() {
        return receberRetorno == 1;
    }

    public void setReceberRetorno(boolean receberRetorno) {
        this.receberRetorno = receberRetorno ? 1 : 0;
    }

    @Transient
    public String getEnviadoEmFormatado() {
        return enviadoEm == null ? "" : enviadoEm.format(DISPLAY_DATE_FORMAT);
    }
}
