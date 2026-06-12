package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.TokenVerificacao;
import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.UsuarioForm;
import br.ufpb.dsc.republica.repository.TokenVerificacaoRepository;
import br.ufpb.dsc.republica.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TokenVerificacaoRepository tokenVerificacaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository, 
                          TokenVerificacaoRepository tokenVerificacaoRepository, 
                          PasswordEncoder passwordEncoder, 
                          EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenVerificacaoRepository = tokenVerificacaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario cadastrar(UsuarioForm form) {
        if (usuarioRepository.existsByEmail(form.email())) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.");
        }

        String senhaCriptografada = passwordEncoder.encode(form.senha());
        Usuario novoUsuario = new Usuario(form.nome(), form.email(), senhaCriptografada);
        novoUsuario.setAtivo(false); // Garantir que inicia inativo para confirmação
        novoUsuario = usuarioRepository.save(novoUsuario);

        // Geração do token de verificação
        String token = UUID.randomUUID().toString();
        TokenVerificacao tokenVerificacao = new TokenVerificacao(token, novoUsuario, 24); // Expira em 24h
        tokenVerificacaoRepository.save(tokenVerificacao);

        // Envio do e-mail
        emailService.enviarEmailConfirmacao(novoUsuario.getEmail(), novoUsuario.getNome(), token);

        return novoUsuario;
    }

    /**
     * Valida o token de confirmação e ativa a conta do usuário correspondente.
     */
    public void verificarEmail(String token) {
        TokenVerificacao tokenVerificacao = tokenVerificacaoRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de verificação inválido."));

        if (tokenVerificacao.estaExpirado()) {
            tokenVerificacaoRepository.delete(tokenVerificacao);
            throw new IllegalArgumentException("O link de verificação expirou. Por favor, solicite um novo cadastro.");
        }

        Usuario usuario = tokenVerificacao.getUsuario();
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);

        // Deleta o token após a verificação
        tokenVerificacaoRepository.delete(tokenVerificacao);
    }
}

