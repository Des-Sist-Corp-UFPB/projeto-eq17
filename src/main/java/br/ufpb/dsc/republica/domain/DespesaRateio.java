package br.ufpb.dsc.republica.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "despesa_rateio", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"despesa_id", "morador_id"})
})
public class DespesaRateio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A despesa é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "despesa_id", nullable = false)
    private Despesa despesa;

    @NotNull(message = "O morador é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "morador_id", nullable = false)
    private Morador morador;

    @NotNull(message = "O valor devido é obrigatório")
    @Column(name = "valor_devido", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDevido;

    @OneToMany(mappedBy = "rateio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pagamento> pagamentos = new ArrayList<>();

    public DespesaRateio() {
    }

    public DespesaRateio(Despesa despesa, Morador morador, BigDecimal valorDevido) {
        this.despesa = despesa;
        this.morador = morador;
        this.valorDevido = valorDevido;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Despesa getDespesa() {
        return despesa;
    }

    public void setDespesa(Despesa despesa) {
        this.despesa = despesa;
    }

    public Morador getMorador() {
        return morador;
    }

    public void setMorador(Morador morador) {
        this.morador = morador;
    }

    public BigDecimal getValorDevido() {
        return valorDevido;
    }

    public void setValorDevido(BigDecimal valorDevido) {
        this.valorDevido = valorDevido;
    }

    public List<Pagamento> getPagamentos() {
        return pagamentos;
    }

    public void setPagamentos(List<Pagamento> pagamentos) {
        this.pagamentos = pagamentos;
    }

    public Pagamento getUltimoPagamento() {
        if (pagamentos.isEmpty()) {
            return null;
        }
        return pagamentos.get(pagamentos.size() - 1);
    }

    @Override
    public String toString() {
        return "DespesaRateio{" +
                "id=" + id +
                ", valorDevido=" + valorDevido +
                '}';
    }
}

