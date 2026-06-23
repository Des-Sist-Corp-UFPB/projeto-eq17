package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.*;
import br.ufpb.dsc.republica.dto.DespesaForm;
import br.ufpb.dsc.republica.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

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
class DespesaServiceTest {

    @Mock
    private DespesaRepository despesaRepository;

    @Mock
    private DespesaRateioRepository despesaRateioRepository;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private CasaRepository casaRepository;

    @Mock
    private MoradorRepository moradorRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private UploadStorageService uploadStorageService;

    @InjectMocks
    private DespesaService despesaService;

    private Casa casa;
    private Usuario usuario;
    private Morador morador;
    private Despesa despesa;
    private DespesaRateio rateio;
    private Pagamento pagamento;
    private DespesaForm despesaForm;

    @BeforeEach
    void setUp() {
        casa = new Casa("República", "Rua Principal");
        casa.setId(5L);
        usuario = new Usuario("Dono", "dono@email.com", "senha");
        usuario.setId(1L);
        morador = new Morador(usuario, casa, PapelMorador.MORADOR);
        morador.setId(10L);
        despesa = new Despesa(casa, "Energia", BigDecimal.valueOf(100.0), LocalDate.now().plusDays(5), morador, StatusDespesa.PENDENTE, TipoDespesa.FIXA, "pix@email.com");
        despesa.setId(100L);
        despesa.setRateios(new ArrayList<>());
        rateio = new DespesaRateio(despesa, morador, BigDecimal.valueOf(100.0));
        rateio.setId(1000L);
        pagamento = new Pagamento(rateio, StatusPagamento.PENDENTE);
        pagamento.setId(2000L);
        rateio.getPagamentos().add(pagamento);
        despesa.getRateios().add(rateio);
        despesaForm = new DespesaForm("Energia", BigDecimal.valueOf(100.0), LocalDate.now().plusDays(5), 10L, TipoDespesa.FIXA, "pix@email.com");
    }

    @Test
    void buscarDespesasPorCasaDeveRetornarLista() {
        when(despesaRepository.findByCasaIdAndExcluidoFalseOrderByVencimentoAsc(5L)).thenReturn(List.of(despesa));
        List<Despesa> result = despesaService.buscarDespesasPorCasa(5L);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Energia", result.get(0).getDescricao());
    }

    @Test
    void buscarPorIdDeveRetornarDespesaSeAtiva() {
        when(despesaRepository.findById(100L)).thenReturn(Optional.of(despesa));
        Despesa result = despesaService.buscarPorId(100L);
        assertNotNull(result);
        assertEquals("Energia", result.getDescricao());
    }

    @org.junit.jupiter.api.Disabled("Diminuindo cobertura para 90% a pedido do usuario")
    @Test
    void buscarPorIdDeveLancarExcecaoSeExcluida() {
        despesa.setExcluido(true);
        when(despesaRepository.findById(100L)).thenReturn(Optional.of(despesa));
        assertThrows(IllegalArgumentException.class, () -> despesaService.buscarPorId(100L));
    }

    @Test
    void buscarPorIdDeveLancarExcecaoSeNaoExistir() {
        when(despesaRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> despesaService.buscarPorId(100L));
    }

    @Test
    void cadastrarDespesaDeveDividirValoresENotificar() {
        when(casaRepository.findById(5L)).thenReturn(Optional.of(casa));
        when(moradorRepository.findById(10L)).thenReturn(Optional.of(morador));
        when(moradorRepository.findByCasaId(5L)).thenReturn(List.of(morador));
        when(despesaRepository.save(any(Despesa.class))).thenReturn(despesa);
        when(despesaRateioRepository.save(any(DespesaRateio.class))).thenReturn(rateio);
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamento);

        Despesa result = despesaService.cadastrarDespesa(5L, despesaForm);

        assertNotNull(result);
        assertEquals("Energia", result.getDescricao());
        verify(notificacaoService, times(1)).criarNotificacao(eq(usuario), anyString(), anyString(), eq(TipoNotificacao.DESPESA_CRIADA));
        verify(auditoriaService, times(1)).registrarAcaoUsuarioLogado(eq("CRIACAO_DESPESA"), anyString(), eq("Despesa"), eq(100L));
    }

    @org.junit.jupiter.api.Disabled("Diminuindo cobertura para 90% a pedido do usuario")
    @Test
    void cadastrarDespesaDeveLancarExcecaoSeCasaNaoEncontrada() {
        when(casaRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> despesaService.cadastrarDespesa(5L, despesaForm));
    }

    @org.junit.jupiter.api.Disabled("Diminuindo cobertura para 90% a pedido do usuario")
    @Test
    void cadastrarDespesaDeveLancarExcecaoSeResponsavelNaoEncontrado() {
        when(casaRepository.findById(5L)).thenReturn(Optional.of(casa));
        when(moradorRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> despesaService.cadastrarDespesa(5L, despesaForm));
    }

    @org.junit.jupiter.api.Disabled("Diminuindo cobertura para 90% a pedido do usuario")
    @Test
    void cadastrarDespesaDeveLancarExcecaoSeResponsavelDeOutraCasa() {
        Casa outraCasa = new Casa("Outra", "End B");
        outraCasa.setId(20L);
        Morador moradorOutra = new Morador(usuario, outraCasa, PapelMorador.MORADOR);
        moradorOutra.setId(10L);

        when(casaRepository.findById(5L)).thenReturn(Optional.of(casa));
        when(moradorRepository.findById(10L)).thenReturn(Optional.of(moradorOutra));

        assertThrows(IllegalArgumentException.class, () -> despesaService.cadastrarDespesa(5L, despesaForm));
    }

    @org.junit.jupiter.api.Disabled("Diminuindo cobertura para 90% a pedido do usuario")
    @Test
    void cadastrarDespesaDeveLancarExcecaoSeCasaNaoTiverMoradores() {
        when(casaRepository.findById(5L)).thenReturn(Optional.of(casa));
        when(moradorRepository.findById(10L)).thenReturn(Optional.of(morador));
        when(moradorRepository.findByCasaId(5L)).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> despesaService.cadastrarDespesa(5L, despesaForm));
    }

    @Test
    void informarPagamentoSemArquivoDeveMudarStatus() {
        when(despesaRateioRepository.findById(1000L)).thenReturn(Optional.of(rateio));

        despesaService.informarPagamento(1000L, "texto-comprovante");

        assertEquals(StatusPagamento.INFORMADO, pagamento.getStatus());
        assertEquals("texto-comprovante", pagamento.getComprovante());
        verify(pagamentoRepository, times(1)).save(pagamento);
    }

    @Test
    void informarPagamentoComArquivoDeveSalvarComprovanteEMudarStatus() {
        MultipartFile arquivo = mock(MultipartFile.class);
        when(arquivo.getOriginalFilename()).thenReturn("comprovante.png");
        when(uploadStorageService.salvarComprovante(arquivo)).thenReturn("uuid-comprovante.png");
        when(despesaRateioRepository.findById(1000L)).thenReturn(Optional.of(rateio));

        despesaService.informarPagamentoComArquivo(1000L, arquivo);

        assertEquals("uuid-comprovante.png", pagamento.getComprovante());
        assertEquals(StatusPagamento.INFORMADO, pagamento.getStatus());
        verify(uploadStorageService, times(1)).salvarComprovante(arquivo);
        verify(pagamentoRepository, times(1)).save(pagamento);
    }

    @Test
    void podeVisualizarComprovanteDeveRetornarTrueParaDonoDoRateio() {
        when(pagamentoRepository.findById(2000L)).thenReturn(Optional.of(pagamento));
        boolean resultado = despesaService.podeVisualizarComprovante(2000L, "dono@email.com");
        assertTrue(resultado);
    }

    @org.junit.jupiter.api.Disabled("Diminuindo cobertura para 90% a pedido do usuario")
    @Test
    void podeVisualizarComprovanteDeveRetornarFalseParaUsuarioNaoAutorizado() {
        when(pagamentoRepository.findById(2000L)).thenReturn(Optional.of(pagamento));
        when(moradorRepository.findByCasaIdAndUsuarioEmail(5L, "intruso@email.com")).thenReturn(Optional.empty());

        boolean resultado = despesaService.podeVisualizarComprovante(2000L, "intruso@email.com");

        assertFalse(resultado);
    }

    @Test
    void confirmarPagamentoDeveMudarStatusParaConfirmadoEAtualizarDespesa() {
        when(pagamentoRepository.findById(2000L)).thenReturn(Optional.of(pagamento));

        despesaService.confirmarPagamento(2000L);

        assertEquals(StatusPagamento.CONFIRMADO, pagamento.getStatus());
        assertEquals(StatusDespesa.PAGA, despesa.getStatus());
        verify(pagamentoRepository, times(1)).save(pagamento);
        verify(despesaRepository, times(1)).save(despesa);
    }

    @Test
    void rejeitarPagamentoDeveMudarStatusParaRejeitadoEAtualizarDespesa() {
        when(pagamentoRepository.findById(2000L)).thenReturn(Optional.of(pagamento));

        despesaService.rejeitarPagamento(2000L);

        assertEquals(StatusPagamento.REJEITADO, pagamento.getStatus());
        assertEquals(StatusDespesa.PENDENTE, despesa.getStatus());
        verify(pagamentoRepository, times(1)).save(pagamento);
        verify(despesaRepository, times(1)).save(despesa);
    }

    @Test
    void excluirDespesaDeveMudarFlagExcluidoParaTrue() {
        when(despesaRepository.findById(100L)).thenReturn(Optional.of(despesa));

        despesaService.excluirDespesa(100L);

        assertTrue(despesa.getExcluido());
        verify(despesaRepository, times(1)).save(despesa);
    }
}
