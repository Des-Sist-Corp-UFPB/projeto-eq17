package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.*;
import br.ufpb.dsc.republica.dto.DespesaForm;
import br.ufpb.dsc.republica.repository.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RepublicaTools {

    private final DespesaService despesaService;
    private final DespesaRepository despesaRepository;
    private final DespesaRateioRepository despesaRateioRepository;
    private final MoradorRepository moradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;
    private final CasaRepository casaRepository;

    public RepublicaTools(DespesaService despesaService,
                          DespesaRepository despesaRepository,
                          DespesaRateioRepository despesaRateioRepository,
                          MoradorRepository moradorRepository,
                          UsuarioRepository usuarioRepository,
                          NotificacaoService notificacaoService,
                          CasaRepository casaRepository) {
        this.despesaService = despesaService;
        this.despesaRepository = despesaRepository;
        this.despesaRateioRepository = despesaRateioRepository;
        this.moradorRepository = moradorRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
        this.casaRepository = casaRepository;
    }

    private void autenticarProgramaticamente(String usuarioEmail) {
        if (usuarioEmail == null || usuarioEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("O e-mail do usuário ('usuarioEmail') é obrigatório para realizar operações de alteração de dados no sistema.");
        }
        Usuario usuario = usuarioRepository.findByEmail(usuarioEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuário com o e-mail '" + usuarioEmail + "' não cadastrado no sistema."));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                usuario.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void validarMoradorPertenceACasa(String usuarioEmail, Long casaId) {
        if (usuarioEmail == null || usuarioEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("O e-mail do usuário ('usuarioEmail') é obrigatório para validar a permissão de acesso à casa.");
        }
        moradorRepository.findByCasaIdAndUsuarioEmail(casaId, usuarioEmail)
                .orElseThrow(() -> new IllegalArgumentException("Acesso negado: o usuário '" + usuarioEmail + "' não é morador cadastrado da casa com ID " + casaId));
    }

    @Tool(description = "Registra uma nova despesa para uma casa/república e divide automaticamente o valor de forma igualitária entre todos os moradores. " +
                        "O parâmetro 'tipo' deve ser 'FIXA' ou 'OCASIONAL'. " +
                        "O vencimento deve estar no formato 'YYYY-MM-DD'.")
    public String registrar_despesa(Long casaId, String descricao, BigDecimal valorTotal, String vencimento,
                                    Long responsavelId, String tipo, String chavePix, String usuarioEmail) {
        try {
            // Autentica o usuário para fins de auditoria
            autenticarProgramaticamente(usuarioEmail);
            validarMoradorPertenceACasa(usuarioEmail, casaId);

            LocalDate dataVencimento;
            try {
                dataVencimento = LocalDate.parse(vencimento);
            } catch (DateTimeParseException e) {
                return "Erro: Data de vencimento '" + vencimento + "' inválida. Use o formato 'YYYY-MM-DD'.";
            }

            TipoDespesa tipoDespesa;
            try {
                tipoDespesa = TipoDespesa.valueOf(tipo.toUpperCase());
            } catch (IllegalArgumentException e) {
                return "Erro: Tipo de despesa '" + tipo + "' inválido. Deve ser 'FIXA' ou 'OCASIONAL'.";
            }

            DespesaForm form = new DespesaForm(descricao, valorTotal, dataVencimento, responsavelId, tipoDespesa, chavePix);
            Despesa despesa = despesaService.cadastrarDespesa(casaId, form);

            return String.format("Sucesso! Despesa '%s' (ID %d) no valor de R$ %.2f cadastrada na casa ID %d. " +
                    "Ela foi dividida entre todos os moradores e o responsável pelo pagamento é o morador ID %d.",
                    despesa.getDescricao(), despesa.getId(), despesa.getValorTotal(), casaId, responsavelId);

        } catch (Exception e) {
            return "Erro ao cadastrar despesa: " + e.getMessage();
        } finally {
            // Limpa o contexto de segurança após a operação
            SecurityContextHolder.clearContext();
        }
    }

    @Tool(description = "Calcula ou busca os rateios de despesas divididas de uma casa em um determinado mês/ano. " +
                        "O parâmetro 'mes' deve estar no formato 'YYYY-MM' (ex: '2026-07').")
    public String dividir_despesas(Long casaId, String mes, String usuarioEmail) {
        try {
            validarMoradorPertenceACasa(usuarioEmail, casaId);

            List<Despesa> despesas = despesaRepository.findByCasaIdAndExcluidoFalseOrderByVencimentoAsc(casaId);
            List<Despesa> despesasDoMes = despesas.stream()
                    .filter(d -> d.getVencimento().toString().startsWith(mes))
                    .collect(Collectors.toList());

            if (despesasDoMes.isEmpty()) {
                return "Nenhuma despesa localizada com vencimento no mês " + mes + " para a casa ID " + casaId + ".";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("### Divisão de Despesas — Mês ").append(mes).append(" (Casa ID ").append(casaId).append(")\n\n");

            BigDecimal totalGeral = BigDecimal.ZERO;

            for (Despesa d : despesasDoMes) {
                sb.append(String.format("- **Despesa:** %s | **Valor Total:** R$ %.2f | **Vencimento:** %s | **Status:** %s\n",
                        d.getDescricao(), d.getValorTotal(), d.getVencimento(), d.getStatus()));
                totalGeral = totalGeral.add(d.getValorTotal());

                sb.append("  *Divisão individual:*\n");
                for (DespesaRateio rateio : d.getRateios()) {
                    StatusPagamento statusPag = rateio.getUltimoPagamento() != null ? rateio.getUltimoPagamento().getStatus() : StatusPagamento.PENDENTE;
                    sb.append(String.format("    * %s: R$ %.2f (%s)\n",
                            rateio.getMorador().getUsuario().getNome(),
                            rateio.getValorDevido(),
                            statusPag));
                }
                sb.append("\n");
            }

            sb.append(String.format("**Valor Total Acumulado no Mês:** R$ %.2f\n", totalGeral));
            return sb.toString();

        } catch (Exception e) {
            return "Erro ao buscar divisão de despesas: " + e.getMessage();
        }
    }

    @Tool(description = "Verifica o saldo devedor em aberto de um morador da república. " +
                        "Pelo menos um identificador ('moradorId', 'emailMorador' ou 'nomeMorador') deve ser fornecido.")
    public String saldo_morador(Long moradorId, String nomeMorador, String emailMorador) {
        try {
            Morador morador = null;

            if (moradorId != null) {
                morador = moradorRepository.findById(moradorId).orElse(null);
            }

            if (morador == null && emailMorador != null && !emailMorador.trim().isEmpty()) {
                List<Morador> todos = moradorRepository.findAll();
                for (Morador m : todos) {
                    if (m.getUsuario().getEmail().equalsIgnoreCase(emailMorador.trim())) {
                        morador = m;
                        break;
                    }
                }
            }

            if (morador == null && nomeMorador != null && !nomeMorador.trim().isEmpty()) {
                List<Morador> todos = moradorRepository.findAll();
                for (Morador m : todos) {
                    if (m.getUsuario().getNome().toLowerCase().contains(nomeMorador.trim().toLowerCase())) {
                        morador = m;
                        break;
                    }
                }
            }

            if (morador == null) {
                return "Erro: Morador não localizado com os dados informados.";
            }

            List<DespesaRateio> rateios = despesaRateioRepository.findByMoradorId(morador.getId());
            List<DespesaRateio> rateiosPendentes = rateios.stream()
                    .filter(r -> !r.getDespesa().getExcluido())
                    .filter(r -> r.getUltimoPagamento() == null || r.getUltimoPagamento().getStatus() != StatusPagamento.CONFIRMADO)
                    .collect(Collectors.toList());

            if (rateiosPendentes.isEmpty()) {
                return String.format("O morador '%s' está com todas as contas em dia! Saldo devedor: R$ 0,00.",
                        morador.getUsuario().getNome());
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("### Saldo Devedor do Morador: %s\n\n", morador.getUsuario().getNome()));
            
            BigDecimal totalPendente = BigDecimal.ZERO;
            for (DespesaRateio r : rateiosPendentes) {
                StatusPagamento status = r.getUltimoPagamento() != null ? r.getUltimoPagamento().getStatus() : StatusPagamento.PENDENTE;
                sb.append(String.format("- **Despesa:** %s | **Parte Devida:** R$ %.2f | **Vencimento:** %s | **Situação do Pagamento:** %s\n",
                        r.getDespesa().getDescricao(),
                        r.getValorDevido(),
                        r.getDespesa().getVencimento(),
                        status));
                totalPendente = totalPendente.add(r.getValorDevido());
            }

            sb.append(String.format("\n**Total Pendente Acumulado:** R$ %.2f\n", totalPendente));
            return sb.toString();

        } catch (Exception e) {
            return "Erro ao verificar saldo do morador: " + e.getMessage();
        }
    }

    @Tool(description = "Dispara uma notificação de aviso geral para todos os moradores de uma casa.")
    public String notificar_moradores(Long casaId, String titulo, String mensagem, String usuarioEmail) {
        try {
            autenticarProgramaticamente(usuarioEmail);
            validarMoradorPertenceACasa(usuarioEmail, casaId);

            List<Morador> moradores = moradorRepository.findByCasaId(casaId);
            if (moradores.isEmpty()) {
                return "Erro: A casa de ID " + casaId + " não possui moradores registrados para receber notificações.";
            }

            int notificacoesEnviadas = 0;
            for (Morador morador : moradores) {
                // Notifica o usuário do morador
                notificacaoService.criarNotificacao(morador.getUsuario(), titulo, mensagem, TipoNotificacao.AVISO);
                notificacoesEnviadas++;
            }

            return String.format("Sucesso! O aviso '%s' foi enviado para %d morador(es) da casa ID %d.",
                    titulo, notificacoesEnviadas, casaId);

        } catch (Exception e) {
            return "Erro ao disparar notificações: " + e.getMessage();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Tool(description = "Fornece o extrato financeiro detalhado de despesas e status de pagamento de uma casa pelo seu ID")
    public String extrato_casa(Long casaId) {
        try {
            Casa casa = casaRepository.findById(casaId)
                    .orElseThrow(() -> new IllegalArgumentException("Casa não encontrada com o ID: " + casaId));

            List<Despesa> despesas = despesaRepository.findByCasaIdAndExcluidoFalseOrderByVencimentoAsc(casaId);

            StringBuilder sb = new StringBuilder();
            sb.append("==================================================\n");
            sb.append("EXTRATO FINANCEIRO — REPÚBLICA: ").append(casa.getNome().toUpperCase()).append("\n");
            sb.append("==================================================\n\n");

            if (despesas.isEmpty()) {
                sb.append("Nenhuma despesa registrada nesta casa até o momento.\n");
                return sb.toString();
            }

            BigDecimal totalPendente = BigDecimal.ZERO;
            BigDecimal totalPago = BigDecimal.ZERO;

            for (Despesa d : despesas) {
                sb.append(String.format("ID: %d | Despesa: %s\n", d.getId(), d.getDescricao()));
                sb.append(String.format("Valor Total: R$ %.2f | Vencimento: %s | Status: %s\n",
                        d.getValorTotal(), d.getVencimento(), d.getStatus()));
                
                sb.append("Rateios detalhados:\n");
                for (DespesaRateio r : d.getRateios()) {
                    StatusPagamento status = r.getUltimoPagamento() != null ? r.getUltimoPagamento().getStatus() : StatusPagamento.PENDENTE;
                    sb.append(String.format("  - %s: R$ %.2f (%s)\n",
                            r.getMorador().getUsuario().getNome(), r.getValorDevido(), status));
                    
                    if (status == StatusPagamento.CONFIRMADO) {
                        totalPago = totalPago.add(r.getValorDevido());
                    } else {
                        totalPendente = totalPendente.add(r.getValorDevido());
                    }
                }
                sb.append("--------------------------------------------------\n");
            }

            sb.append("\n=================== BALANÇO GERAL ===================\n");
            sb.append(String.format("Total de Despesas Confirmadas/Pagas: R$ %.2f\n", totalPago));
            sb.append(String.format("Total de Despesas Pendentes/Abertas: R$ %.2f\n", totalPendente));
            sb.append(String.format("Volume Total Lançado no Histórico: R$ %.2f\n", totalPago.add(totalPendente)));
            sb.append("=====================================================\n");

            return sb.toString();

        } catch (Exception e) {
            return "Erro ao carregar o extrato da casa: " + e.getMessage();
        }
    }
}
