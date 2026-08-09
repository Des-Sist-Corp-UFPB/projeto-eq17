package br.ufpb.dsc.republica.domain;

/**
 * Define o status de pagamento de uma despesa da república.
 */
public enum StatusDespesa {
    PENDENTE("Pendente"),
    PARCIALMENTE_PAGA("Parcialmente Paga"),
    PAGA("Paga"),
    ATRASADA("Atrasada");

    private final String descricao;

    StatusDespesa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

