package com.alphabetz.webalphabetz.model;

public enum CargoEquipe {

    PRESIDENTE_CONSELHO("Presidente do Conselho"),
    DIRETOR_EXECUTIVO_CEO("Diretor Executivo / CEO"),
    DIRETORA_ENSINO("Diretora de Ensino"),
    COORDENADORA_PEDAGOGICA("Coordenadora Pedagógica"),
    ASSISTENTE_ADMINISTRATIVO("Assistente Administrativo"),
    PROFESSORA_REGENTE("Professora Regente"),
    PROFESSORA_INGLES("Professora de Inglês"),
    AUXILIAR_SALA("Auxiliar de Sala"),
    BERCARISTA("Berçarista"),
    ATELIERISTA("Atelierista"),
    PORTEIRO("Porteiro"),
    AUXILIAR_SERVICOS_GERAIS("Auxiliar de Serviços Gerais"),
    AUXILIAR_COZINHA("Auxiliar de Cozinha");

    private final String descricao;

    CargoEquipe(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
