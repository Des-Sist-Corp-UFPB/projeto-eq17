package br.ufpb.dsc.republica.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "despesa")
public class Despesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A casa é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casa_id", nullable = false)
    private Casa casa;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 200, message = "A descrição pode ter no máximo 200 caracteres")
    @Column(name = "descricao", nullable = false, length = 200)
    private String descricao;

    @NotNull(message = "O valor total é obrigatório")
    @DecimalMin(value = "0.00", message = "O valor não pode ser negativo")
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @NotNull(message = "A data de vencimento é obrigatória")
    @Column(name = "vencimento", nullable = false)
    private LocalDate vencimento;

    @NotNull(message = "O responsável pelo pagamento é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Morador responsavel;

    @NotNull(message = "O status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private StatusDespesa status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Column(name = "excluido", nullable = false)
    private Boolean excluido = false;

    @OneToMany(mappedBy = "despesa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DespesaRateio> rateios = new ArrayList<>();

    @PrePersist
    protected void prePersist() {
        Instant agora = Instant.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
        if (this.excluido == null) {
            this.excluido = false;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        this.atualizadoEm = Instant.now();
    }

    public Despesa() {
    }

    public Despesa(Casa casa, String descricao, BigDecimal valorTotal, LocalDate vencimento, Morador responsavel, StatusDespesa status) {
        this.casa = casa;
        this.descricao = descricao;
        this.valorTotal = valorTotal;
        this.vencimento = vencimento;
        this.responsavel = responsavel;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Casa getCasa() {
        return casa;
    }

    public void setCasa(Casa casa) {
        this.casa = casa;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
    }

    public Morador getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(Morador responsavel) {
        this.responsavel = responsavel;
    }

    public StatusDespesa getStatus() {
        return status;
    }

    public void setStatus(StatusDespesa status) {
        this.status = status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Instant atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public Boolean getExcluido() {
        return excluido;
    }

    public void setExcluido(Boolean excluido) {
        this.excluido = excluido;
    }

    public List<DespesaRateio> getRateios() {
        return rateios;
    }

    public void setRateios(List<DespesaRateio> rateios) {
        this.rateios = rateios;
    }

    @Override
    public String toString() {
        return "Despesa{" +
                "id=" + id +
                ", descricao='" + descricao + '\'' +
                ", valorTotal=" + valorTotal +
                ", status=" + status +
                '}';
    }
}

