package br.ufpb.dsc.republica.domain;

/**
 * Define o papel de um morador dentro de uma república/casa.
 */
public enum PapelMorador {
    ADMINISTRADOR("Administrador"),
    MORADOR("Morador");

    private final String descricao;

    PapelMorador(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

