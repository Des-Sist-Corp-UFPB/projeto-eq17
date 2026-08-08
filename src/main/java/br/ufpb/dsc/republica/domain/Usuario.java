package br.ufpb.dsc.republica.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres")
    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 150, message = "O e-mail pode ter no máximo 150 caracteres")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
    @Column(name = "senha", nullable = false, length = 100)
    private String senha;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "aceitou_termos_lgpd", nullable = false)
    private Boolean aceitouTermosLgpd = false;

    @Column(name = "data_aceite_lgpd")
    private Instant dataAceiteLgpd;

    @Column(name = "versao_termo_lgpd", length = 50)
    private String versaoTermoLgpd;

    @Column(name = "email_confirmado", nullable = false)
    private Boolean emailConfirmado = false;

    @Column(name = "token_confirmacao", length = 100)
    private String tokenConfirmacao;

    @Column(name = "token_redefinicao_senha", length = 100)
    private String tokenRedefinicaoSenha;

    @Column(name = "validade_token_redefinicao")
    private Instant validadeTokenRedefinicao;

    @PrePersist
    protected void prePersist() {
        this.criadoEm = Instant.now();
    }

    public Usuario() {
    }

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Boolean getAceitouTermosLgpd() {
        return aceitouTermosLgpd;
    }

    public void setAceitouTermosLgpd(Boolean aceitouTermosLgpd) {
        this.aceitouTermosLgpd = aceitouTermosLgpd;
    }

    public Instant getDataAceiteLgpd() {
        return dataAceiteLgpd;
    }

    public void setDataAceiteLgpd(Instant dataAceiteLgpd) {
        this.dataAceiteLgpd = dataAceiteLgpd;
    }

    public String getVersaoTermoLgpd() {
        return versaoTermoLgpd;
    }

    public void setVersaoTermoLgpd(String versaoTermoLgpd) {
        this.versaoTermoLgpd = versaoTermoLgpd;
    }

    public Boolean getEmailConfirmado() {
        return emailConfirmado;
    }

    public void setEmailConfirmado(Boolean emailConfirmado) {
        this.emailConfirmado = emailConfirmado;
    }

    public String getTokenConfirmacao() {
        return tokenConfirmacao;
    }

    public void setTokenConfirmacao(String tokenConfirmacao) {
        this.tokenConfirmacao = tokenConfirmacao;
    }

    public String getTokenRedefinicaoSenha() {
        return tokenRedefinicaoSenha;
    }

    public void setTokenRedefinicaoSenha(String tokenRedefinicaoSenha) {
        this.tokenRedefinicaoSenha = tokenRedefinicaoSenha;
    }

    public Instant getValidadeTokenRedefinicao() {
        return validadeTokenRedefinicao;
    }

    public void setValidadeTokenRedefinicao(Instant validadeTokenRedefinicao) {
        this.validadeTokenRedefinicao = validadeTokenRedefinicao;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

