package br.ufpb.dsc.republica.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @NotBlank(message = "A ação é obrigatória")
    @Column(name = "acao", nullable = false, length = 100)
    private String acao;

    @NotBlank(message = "A descrição é obrigatória")
    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "A data e hora são obrigatórias")
    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    @Column(name = "endereco_ip", length = 45)
    private String enderecoIp;

    @Column(name = "entidade_afetada", length = 100)
    private String entidadeAfetada;

    @Column(name = "entidade_id")
    private Long entidadeId;

    @PrePersist
    protected void prePersist() {
        this.dataHora = Instant.now();
    }

    public Auditoria() {
    }

    public Auditoria(Usuario usuario, String acao, String descricao, String enderecoIp, String entidadeAfetada, Long entidadeId) {
        this.usuario = usuario;
        this.acao = acao;
        this.descricao = descricao;
        this.enderecoIp = enderecoIp;
        this.entidadeAfetada = entidadeAfetada;
        this.entidadeId = entidadeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Instant getDataHora() {
        return dataHora;
    }

    public void setDataHora(Instant dataHora) {
        this.dataHora = dataHora;
    }

    public String getEnderecoIp() {
        return enderecoIp;
    }

    public void setEnderecoIp(String enderecoIp) {
        this.enderecoIp = enderecoIp;
    }

    public String getEntidadeAfetada() {
        return entidadeAfetada;
    }

    public void setEntidadeAfetada(String entidadeAfetada) {
        this.entidadeAfetada = entidadeAfetada;
    }

    public Long getEntidadeId() {
        return entidadeId;
    }

    public void setEntidadeId(Long entidadeId) {
        this.entidadeId = entidadeId;
    }

    @Override
    public String toString() {
        return "Auditoria{" +
                "id=" + id +
                ", acao='" + acao + '\'' +
                ", dataHora=" + dataHora +
                '}';
    }
}
