package br.ufpb.dsc.republica.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "token_verificacao")
public class TokenVerificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "token", nullable = false, unique = true, length = 100)
    private String token;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull
    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void prePersist() {
        this.criadoEm = Instant.now();
    }

    public TokenVerificacao() {
    }

    public TokenVerificacao(String token, Usuario usuario, int expiraEmHoras) {
        this.token = token;
        this.usuario = usuario;
        this.expiraEm = Instant.now().plus(java.time.Duration.ofHours(expiraEmHoras));
    }

    public boolean estaExpirado() {
        return Instant.now().isAfter(this.expiraEm);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(Instant expiraEm) {
        this.expiraEm = expiraEm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }
}
