package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.*;
import br.ufpb.dsc.republica.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    @Test
    void informarPagamentoComArquivoDeveSalvarComprovanteEMudarStatus() {
        Long rateioId = 1L;
        MultipartFile arquivo = mock(MultipartFile.class);
        when(arquivo.getOriginalFilename()).thenReturn("comprovante.pdf");
        when(uploadStorageService.salvarComprovante(arquivo)).thenReturn("uuid-comprovante.pdf");

        Usuario usuario = new Usuario("João", "joao@email.com", "senha");
        Morador morador = new Morador(usuario, new Casa(), PapelMorador.MORADOR);
        
        Despesa despesa = new Despesa();
        despesa.setRateios(new ArrayList<>());
        
        DespesaRateio rateio = new DespesaRateio(despesa, morador, BigDecimal.TEN);
        rateio.setId(rateioId);
        despesa.getRateios().add(rateio);

        Pagamento pagamento = new Pagamento(rateio, StatusPagamento.PENDENTE);
        rateio.getPagamentos().add(pagamento);

        when(despesaRateioRepository.findById(rateioId)).thenReturn(Optional.of(rateio));

        despesaService.informarPagamentoComArquivo(rateioId, arquivo);

        assertEquals("uuid-comprovante.pdf", pagamento.getComprovante());
        assertEquals(StatusPagamento.INFORMADO, pagamento.getStatus());
        verify(uploadStorageService, times(1)).salvarComprovante(arquivo);
        verify(pagamentoRepository, times(1)).save(pagamento);
        verify(auditoriaService, times(1)).registrarAcaoUsuarioLogado(eq("INFORMAR_PAGAMENTO"), anyString(), eq("DespesaRateio"), eq(rateioId));
    }

    @Test
    void podeVisualizarComprovanteDeveRetornarTrueParaDonoDoRateio() {
        Long pagamentoId = 100L;
        String emailDono = "joao@email.com";

        Usuario usuario = new Usuario("João", emailDono, "senha");
        Morador morador = new Morador(usuario, new Casa(), PapelMorador.MORADOR);
        Despesa despesa = new Despesa();
        despesa.setResponsavel(new Morador(new Usuario("Outro", "outro@email.com", "senha"), new Casa(), PapelMorador.MORADOR));
        
        DespesaRateio rateio = new DespesaRateio(despesa, morador, BigDecimal.TEN);
        Pagamento pagamento = new Pagamento(rateio, StatusPagamento.INFORMADO);
        pagamento.setId(pagamentoId);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));

        boolean resultado = despesaService.podeVisualizarComprovante(pagamentoId, emailDono);

        assertTrue(resultado);
    }

    @Test
    void podeVisualizarComprovanteDeveRetornarTrueParaResponsavelDaDespesa() {
        Long pagamentoId = 100L;
        String emailResponsavel = "resp@email.com";

        Usuario usuarioDono = new Usuario("João", "joao@email.com", "senha");
        Morador moradorDono = new Morador(usuarioDono, new Casa(), PapelMorador.MORADOR);

        Usuario usuarioResp = new Usuario("Responsavel", emailResponsavel, "senha");
        Morador moradorResp = new Morador(usuarioResp, new Casa(), PapelMorador.ADMINISTRADOR);

        Despesa despesa = new Despesa();
        despesa.setResponsavel(moradorResp);

        DespesaRateio rateio = new DespesaRateio(despesa, moradorDono, BigDecimal.TEN);
        Pagamento pagamento = new Pagamento(rateio, StatusPagamento.INFORMADO);
        pagamento.setId(pagamentoId);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));

        boolean resultado = despesaService.podeVisualizarComprovante(pagamentoId, emailResponsavel);

        assertTrue(resultado);
    }

    @Test
    void podeVisualizarComprovanteDeveRetornarTrueParaAdministradorDaCasa() {
        Long pagamentoId = 100L;
        String emailAdm = "admin@email.com";
        Casa casa = new Casa();
        casa.setId(5L);

        Usuario usuarioDono = new Usuario("João", "joao@email.com", "senha");
        Morador moradorDono = new Morador(usuarioDono, casa, PapelMorador.MORADOR);

        Usuario usuarioResp = new Usuario("Responsavel", "resp@email.com", "senha");
        Morador moradorResp = new Morador(usuarioResp, casa, PapelMorador.MORADOR);

        Despesa despesa = new Despesa();
        despesa.setCasa(casa);
        despesa.setResponsavel(moradorResp);

        DespesaRateio rateio = new DespesaRateio(despesa, moradorDono, BigDecimal.TEN);
        Pagamento pagamento = new Pagamento(rateio, StatusPagamento.INFORMADO);
        pagamento.setId(pagamentoId);

        Usuario usuarioAdm = new Usuario("Admin", emailAdm, "senha");
        Morador moradorAdm = new Morador(usuarioAdm, casa, PapelMorador.ADMINISTRADOR);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(moradorRepository.findByCasaIdAndUsuarioEmail(5L, emailAdm)).thenReturn(Optional.of(moradorAdm));

        boolean resultado = despesaService.podeVisualizarComprovante(pagamentoId, emailAdm);

        assertTrue(resultado);
    }

    @Test
    void podeVisualizarComprovanteDeveRetornarFalseParaUsuarioNaoAutorizado() {
        Long pagamentoId = 100L;
        String emailIntruso = "intruso@email.com";
        Casa casa = new Casa();
        casa.setId(5L);

        Usuario usuarioDono = new Usuario("João", "joao@email.com", "senha");
        Morador moradorDono = new Morador(usuarioDono, casa, PapelMorador.MORADOR);

        Usuario usuarioResp = new Usuario("Responsavel", "resp@email.com", "senha");
        Morador moradorResp = new Morador(usuarioResp, casa, PapelMorador.MORADOR);

        Despesa despesa = new Despesa();
        despesa.setCasa(casa);
        despesa.setResponsavel(moradorResp);

        DespesaRateio rateio = new DespesaRateio(despesa, moradorDono, BigDecimal.TEN);
        Pagamento pagamento = new Pagamento(rateio, StatusPagamento.INFORMADO);
        pagamento.setId(pagamentoId);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(moradorRepository.findByCasaIdAndUsuarioEmail(5L, emailIntruso)).thenReturn(Optional.empty());

        boolean resultado = despesaService.podeVisualizarComprovante(pagamentoId, emailIntruso);

        assertFalse(resultado);
    }
}
