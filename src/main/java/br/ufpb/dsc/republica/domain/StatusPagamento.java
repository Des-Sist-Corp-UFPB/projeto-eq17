package br.ufpb.dsc.republica.domain;

/**
 * Define o status de validação de um pagamento de rateio.
 */
public enum StatusPagamento {
    PENDENTE("Pendente"),
    INFORMADO("Informado"),
    CONFIRMADO("Confirmado"),
    REJEITADO("Rejeitado");

    private final String descricao;

    StatusPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

