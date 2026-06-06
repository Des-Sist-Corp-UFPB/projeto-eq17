package br.ufpb.dsc.republica.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O rateio da despesa é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rateio_id", nullable = false)
    private DespesaRateio rateio;

    @Column(name = "data_pagamento")
    private Instant dataPagamento;

    @Column(name = "comprovante", length = 255)
    private String comprovante;

    @NotNull(message = "O status do pagamento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private StatusPagamento status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant updatedEm;

    @PrePersist
    protected void prePersist() {
        Instant agora = Instant.now();
        this.criadoEm = agora;
        this.updatedEm = agora;
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedEm = Instant.now();
    }

    public Pagamento() {
    }

    public Pagamento(DespesaRateio rateio, StatusPagamento status) {
        this.rateio = rateio;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DespesaRateio getRateio() {
        return rateio;
    }

    public void setRateio(DespesaRateio rateio) {
        this.rateio = rateio;
    }

    public Instant getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Instant dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public String getComprovante() {
        return comprovante;
    }

    public void setComprovante(String comprovante) {
        this.comprovante = comprovante;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Instant getUpdatedEm() {
        return updatedEm;
    }

    public void setUpdatedEm(Instant updatedEm) {
        this.updatedEm = updatedEm;
    }

    @Override
    public String toString() {
        return "Pagamento{" +
                "id=" + id +
                ", status=" + status +
                '}';
    }
}

