package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.*;
import br.ufpb.dsc.republica.dto.LgpdExportDto;
import br.ufpb.dsc.republica.dto.UsuarioForm;
import br.ufpb.dsc.republica.repository.DespesaRateioRepository;
import br.ufpb.dsc.republica.repository.MoradorRepository;
import br.ufpb.dsc.republica.repository.TarefaRepository;
import br.ufpb.dsc.republica.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final MoradorRepository moradorRepository;
    private final TarefaRepository tarefaRepository;
    private final DespesaRateioRepository despesaRateioRepository;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          MoradorRepository moradorRepository,
                          TarefaRepository tarefaRepository,
                          DespesaRateioRepository despesaRateioRepository,
                          AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.moradorRepository = moradorRepository;
        this.tarefaRepository = tarefaRepository;
        this.despesaRateioRepository = despesaRateioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario cadastrar(UsuarioForm form) {
        if (usuarioRepository.existsByEmail(form.email())) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.");
        }

        // Validação de consentimento de uso dos dados para LGPD
        if (form.aceitouTermosLgpd() == null || !form.aceitouTermosLgpd()) {
            throw new IllegalArgumentException("É obrigatório aceitar a Política de Privacidade e os Termos de Uso do sistema.");
        }

        String senhaCriptografada = passwordEncoder.encode(form.senha());
        Usuario novoUsuario = new Usuario(form.nome(), form.email(), senhaCriptografada);
        
        // Atribui os campos de consentimento
        novoUsuario.setAceitouTermosLgpd(true);
        novoUsuario.setDataAceiteLgpd(Instant.now());
        novoUsuario.setVersaoTermoLgpd(form.versaoTermoLgpd() != null ? form.versaoTermoLgpd() : "1.0");

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        // Registra o cadastro na Auditoria
        auditoriaService.registrar(usuarioSalvo, "CADASTRO", "Usuário cadastrado com sucesso aceitando os termos da LGPD na versão " + usuarioSalvo.getVersaoTermoLgpd(), "Usuario", usuarioSalvo.getId());

        return usuarioSalvo;
    }

    @Transactional
    public Usuario registrarOuObterUsuarioOAuth2(String email, String nome) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            auditoriaService.registrar(usuario, "LOGIN", "Login realizado com sucesso via Google OAuth2.", "Usuario", usuario.getId());
            return usuario;
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setEmail(email);
        novoUsuario.setNome(nome != null ? nome : email.split("@")[0]);

        String senhaAleatoria = UUID.randomUUID().toString();
        novoUsuario.setSenha(passwordEncoder.encode(senhaAleatoria));

        novoUsuario.setAceitouTermosLgpd(true);
        novoUsuario.setDataAceiteLgpd(Instant.now());
        novoUsuario.setVersaoTermoLgpd("1.0 (Google OAuth2)");

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        auditoriaService.registrar(usuarioSalvo, "CADASTRO", "Usuário cadastrado automaticamente via Google OAuth2 com termos da LGPD na versão 1.0 (Google OAuth2)", "Usuario", usuarioSalvo.getId());
        auditoriaService.registrar(usuarioSalvo, "LOGIN", "Login realizado com sucesso via Google OAuth2.", "Usuario", usuarioSalvo.getId());

        return usuarioSalvo;
    }

    @Transactional(readOnly = true)
    public boolean possuiPendenciasFinanceiras(Long usuarioId) {
        List<Morador> moradores = moradorRepository.findByUsuarioId(usuarioId);
        for (Morador morador : moradores) {
            List<DespesaRateio> rateios = despesaRateioRepository.findByMoradorId(morador.getId());
            for (DespesaRateio rateio : rateios) {
                if (rateio.getDespesa().getExcluido()) {
                    continue;
                }
                Pagamento ultimoPagamento = rateio.getUltimoPagamento();
                if (ultimoPagamento == null || ultimoPagamento.getStatus() != StatusPagamento.CONFIRMADO) {
                    return true;
                }
            }
        }
        return false;
    }

    public void excluirUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (possuiPendenciasFinanceiras(usuarioId)) {
            // Se possuir despesas pendentes ou responsabilidades financeiras em aberto, executa anonimização
            String nomeAntigo = usuario.getNome();
            String emailAntigo = usuario.getEmail();

            usuario.setNome("Usuario Removido #" + usuarioId);
            usuario.setEmail("removido" + usuarioId + "@sistema.local");
            usuario.setSenha(passwordEncoder.encode(UUID.randomUUID().toString())); // Invalida a senha
            usuario.setAceitouTermosLgpd(false);
            usuario.setDataAceiteLgpd(null);
            usuario.setVersaoTermoLgpd(null);

            usuarioRepository.save(usuario);

            // Grava na auditoria
            auditoriaService.registrar(usuario, "SOLICITACAO_EXCLUSAO", 
                String.format("Usuário '%s' (%s) foi anonimizado por solicitação devido a despesas/pagamentos em aberto.", nomeAntigo, emailAntigo), 
                "Usuario", usuarioId);
        } else {
            // Caso contrário, realiza a exclusão física do registro
            // Primeiro gera o registro de auditoria órfão de usuário para segurança
            auditoriaService.registrar(null, "SOLICITACAO_EXCLUSAO", 
                String.format("Usuário '%s' (%s) excluído fisicamente de forma permanente do sistema.", usuario.getNome(), usuario.getEmail()), 
                "Usuario", usuarioId);
            
            usuarioRepository.delete(usuario);
        }
    }

    @Transactional
    public LgpdExportDto exportarDados(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        List<Morador> moradores = moradorRepository.findByUsuarioId(usuarioId);

        // Mapeia Casas das quais o usuário participa
        List<LgpdExportDto.CasaDados> casas = moradores.stream()
                .map(m -> new LgpdExportDto.CasaDados(
                        m.getCasa().getId(),
                        m.getCasa().getNome(),
                        m.getCasa().getEndereco(),
                        m.getPapel().name(),
                        m.getCasa().getCriadoEm()
                )).toList();

        List<LgpdExportDto.DespesaDados> despesas = new ArrayList<>();
        List<LgpdExportDto.PagamentoDados> pagamentos = new ArrayList<>();
        List<LgpdExportDto.TarefaDados> tarefas = new ArrayList<>();

        for (Morador morador : moradores) {
            // Histórico de tarefas atribuídas a este morador
            List<Tarefa> tarefasMorador = tarefaRepository.findByResponsavelId(morador.getId());
            for (Tarefa t : tarefasMorador) {
                tarefas.add(new LgpdExportDto.TarefaDados(
                        t.getId(),
                        t.getCasa().getId(),
                        t.getCasa().getNome(),
                        t.getDescricao(),
                        t.getStatus().name(),
                        t.getCriadoEm(),
                        t.getAtualizadoEm()
                ));
            }

            // Histórico de despesas e pagamentos deste morador
            List<DespesaRateio> rateios = despesaRateioRepository.findByMoradorId(morador.getId());
            for (DespesaRateio rateio : rateios) {
                Despesa despesa = rateio.getDespesa();
                
                despesas.add(new LgpdExportDto.DespesaDados(
                        despesa.getId(),
                        despesa.getCasa().getId(),
                        despesa.getCasa().getNome(),
                        despesa.getDescricao(),
                        despesa.getValorTotal(),
                        despesa.getVencimento(),
                        despesa.getStatus().name(),
                        despesa.getTipo().name(),
                        despesa.getChavePix(),
                        despesa.getCriadoEm()
                ));

                for (Pagamento pag : rateio.getPagamentos()) {
                    pagamentos.add(new LgpdExportDto.PagamentoDados(
                            pag.getId(),
                            rateio.getId(),
                            despesa.getId(),
                            despesa.getDescricao(),
                            rateio.getValorDevido(),
                            pag.getDataPagamento(),
                            pag.getComprovante(),
                            pag.getStatus().name(),
                            pag.getCriadoEm()
                    ));
                }
            }
        }

        // Histórico de auditoria do usuário
        List<Auditoria> auditoriasLista = auditoriaService.buscarPorUsuario(usuarioId);
        List<LgpdExportDto.AuditoriaDados> auditorias = auditoriasLista.stream()
                .map(a -> new LgpdExportDto.AuditoriaDados(
                        a.getId(),
                        a.getAcao(),
                        a.getDescricao(),
                        a.getDataHora(),
                        a.getEnderecoIp(),
                        a.getEntidadeAfetada(),
                        a.getEntidadeId()
                )).toList();

        LgpdExportDto.UsuarioDados cadastral = new LgpdExportDto.UsuarioDados(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCriadoEm(),
                usuario.getAceitouTermosLgpd(),
                usuario.getDataAceiteLgpd(),
                usuario.getVersaoTermoLgpd()
        );

        // Registra a exportação na Auditoria
        auditoriaService.registrar(usuario, "EXPORTACAO_DADOS", "Dados cadastrais e histórico completo exportados pelo usuário.", "Usuario", usuarioId);

        return new LgpdExportDto(cadastral, casas, despesas, pagamentos, tarefas, auditorias);
    }
}
