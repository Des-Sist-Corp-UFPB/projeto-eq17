package br.ufpb.dsc.republica.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "morador", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"usuario_id", "casa_id"})
})
public class Morador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O usuário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull(message = "A casa é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casa_id", nullable = false)
    private Casa casa;

    @NotNull(message = "O papel do morador é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "papel", nullable = false, length = 50)
    private PapelMorador papel;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void prePersist() {
        this.criadoEm = Instant.now();
    }

    public Morador() {
    }

    public Morador(Usuario usuario, Casa casa, PapelMorador papel) {
        this.usuario = usuario;
        this.casa = casa;
        this.papel = papel;
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

    public Casa getCasa() {
        return casa;
    }

    public void setCasa(Casa casa) {
        this.casa = casa;
    }

    public PapelMorador getPapel() {
        return papel;
    }

    public void setPapel(PapelMorador papel) {
        this.papel = papel;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    @Override
    public String toString() {
        return "Morador{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getNome() : null) +
                ", papel=" + papel +
                '}';
    }
}
