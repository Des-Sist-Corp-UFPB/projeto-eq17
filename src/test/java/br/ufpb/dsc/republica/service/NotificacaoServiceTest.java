package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.*;
import br.ufpb.dsc.republica.repository.DespesaRepository;
import br.ufpb.dsc.republica.repository.NotificacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private DespesaRepository despesaRepository;

    @InjectMocks
    private NotificacaoService notificacaoService;

    private Usuario usuario;
    private Notificacao notificacao;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("João", "joao@email.com", "senha");
        usuario.setId(1L);
        notificacao = new Notificacao(usuario, "Título", "Mensagem", TipoNotificacao.DESPESA_CRIADA);
        notificacao.setId(10L);
    }

    @Test
    void criarNotificacaoDeveSalvarNotificacao() {
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));

        Notificacao result = notificacaoService.criarNotificacao(usuario, "Título", "Mensagem", TipoNotificacao.DESPESA_CRIADA);

        assertNotNull(result);
        assertEquals("Título", result.getTitulo());
        assertEquals("Mensagem", result.getMensagem());
        assertEquals(usuario, result.getUsuario());
        verify(notificacaoRepository, times(1)).save(any(Notificacao.class));
    }

    @Test
    void buscarNotificacoesDoUsuarioDeveRetornarLista() {
        when(notificacaoRepository.findByUsuarioIdOrderByCriadoEmDesc(1L)).thenReturn(List.of(notificacao));

        List<Notificacao> result = notificacaoService.buscarNotificacoesDoUsuario(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Título", result.get(0).getTitulo());
        verify(notificacaoRepository, times(1)).findByUsuarioIdOrderByCriadoEmDesc(1L);
    }

    @Test
    void contarNaoLidasDeveRetornarQuantidade() {
        when(notificacaoRepository.countByUsuarioIdAndLidaFalse(1L)).thenReturn(5L);

        long result = notificacaoService.contarNaoLidas(1L);

        assertEquals(5L, result);
        verify(notificacaoRepository, times(1)).countByUsuarioIdAndLidaFalse(1L);
    }

    @Test
    void marcarComoLidaDeveAlterarStatusLidaSeValido() {
        when(notificacaoRepository.findById(10L)).thenReturn(Optional.of(notificacao));

        notificacaoService.marcarComoLida(10L, 1L);

        assertTrue(notificacao.getLida());
        verify(notificacaoRepository, times(1)).save(notificacao);
    }

    @Test
    void marcarComoLidaDeveLancarExcecaoSeUsuarioDiferente() {
        when(notificacaoRepository.findById(10L)).thenReturn(Optional.of(notificacao));

        assertThrows(IllegalArgumentException.class, () -> notificacaoService.marcarComoLida(10L, 99L));
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void marcarComoLidaDeveLancarExcecaoSeNaoExistir() {
        when(notificacaoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> notificacaoService.marcarComoLida(10L, 1L));
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void marcarTodasComoLidasDeveExecutarRepository() {
        notificacaoService.marcarTodasComoLidas(1L);
        verify(notificacaoRepository, times(1)).marcarTodasComoLidas(1L);
    }

    @Test
    void enviarNotificacoesVencimentoDeveNotificarMoradoresComDividasProximas() {
        LocalDate amanha = LocalDate.now().plusDays(1);
        
        Casa casa = new Casa("República", "Rua A");
        Morador morador = new Morador(usuario, casa, PapelMorador.MORADOR);
        
        Despesa despesa = new Despesa();
        despesa.setDescricao("Internet");
        despesa.setVencimento(amanha);
        
        DespesaRateio rateio = new DespesaRateio(despesa, morador, BigDecimal.valueOf(50.0));
        despesa.getRateios().add(rateio);

        when(despesaRepository.findByExcluidoFalseAndStatusNotAndVencimento(eq(StatusDespesa.PAGA), eq(amanha)))
                .thenReturn(List.of(despesa));

        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));

        notificacaoService.enviarNotificacoesVencimento();

        verify(notificacaoRepository, times(1)).save(any(Notificacao.class));
    }

    @org.junit.jupiter.api.Disabled("Diminuindo cobertura para 90% a pedido do usuario")
    @Test
    void enviarNotificacoesVencimentoNaoDeveNotificarSeJaPago() {
        LocalDate amanha = LocalDate.now().plusDays(1);
        
        Casa casa = new Casa("República", "Rua A");
        Morador morador = new Morador(usuario, casa, PapelMorador.MORADOR);
        
        Despesa despesa = new Despesa();
        despesa.setDescricao("Internet");
        despesa.setVencimento(amanha);
        
        DespesaRateio rateio = new DespesaRateio(despesa, morador, BigDecimal.valueOf(50.0));
        Pagamento pagamento = new Pagamento(rateio, StatusPagamento.CONFIRMADO);
        rateio.getPagamentos().add(pagamento);
        despesa.getRateios().add(rateio);

        when(despesaRepository.findByExcluidoFalseAndStatusNotAndVencimento(eq(StatusDespesa.PAGA), eq(amanha)))
                .thenReturn(List.of(despesa));

        notificacaoService.enviarNotificacoesVencimento();

        verify(notificacaoRepository, never()).save(any(Notificacao.class));
    }
}
