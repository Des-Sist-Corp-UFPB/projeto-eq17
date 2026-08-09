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
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RepublicaToolsTest {

    @Mock
    private DespesaService despesaService;
    @Mock
    private DespesaRepository despesaRepository;
    @Mock
    private DespesaRateioRepository despesaRateioRepository;
    @Mock
    private MoradorRepository moradorRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private CasaRepository casaRepository;

    @InjectMocks
    private RepublicaTools republicaTools;

    private Usuario usuario;
    private Casa casa;
    private Morador morador;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("Ramon", "ramon@test.com", "senha123");
        usuario.setEmailConfirmado(true);
        casa = new Casa("Casa Teste", "Rua das Flores, 123");
        casa.setId(1L);
        morador = new Morador(usuario, casa, PapelMorador.ADMINISTRADOR);
        morador.setId(10L);
    }


    @Test
    void registrarDespesaComSucesso() {
        when(usuarioRepository.findByEmail("ramon@test.com")).thenReturn(Optional.of(usuario));
        when(moradorRepository.findByCasaIdAndUsuarioEmail(1L, "ramon@test.com")).thenReturn(Optional.of(morador));
        
        Despesa despesaSalva = new Despesa(casa, "Energia", BigDecimal.valueOf(150.0), LocalDate.parse("2026-07-30"), morador, StatusDespesa.PENDENTE, TipoDespesa.FIXA, "chave-pix");
        despesaSalva.setId(100L);
        
        when(despesaService.cadastrarDespesa(any(Long.class), any(DespesaForm.class))).thenReturn(despesaSalva);

        String result = republicaTools.registrar_despesa(1L, "Energia", BigDecimal.valueOf(150.0), "2026-07-30", 10L, "FIXA", "chave-pix", "ramon@test.com");

        assertTrue(result.contains("Sucesso!"));
        assertTrue(result.contains("Energia"));
        assertTrue(result.contains("ID 100"));
        verify(despesaService, times(1)).cadastrarDespesa(eq(1L), any(DespesaForm.class));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void registrarDespesaDataInvalida() {
        when(usuarioRepository.findByEmail("ramon@test.com")).thenReturn(Optional.of(usuario));
        when(moradorRepository.findByCasaIdAndUsuarioEmail(1L, "ramon@test.com")).thenReturn(Optional.of(morador));

        String result = republicaTools.registrar_despesa(1L, "Energia", BigDecimal.valueOf(150.0), "30-07-2026", 10L, "FIXA", "chave-pix", "ramon@test.com");

        assertTrue(result.contains("Erro"));
        assertTrue(result.contains("formato 'YYYY-MM-DD'"));
        verify(despesaService, never()).cadastrarDespesa(any(), any());
    }


    @Test
    void registrarDespesaTipoInvalido() {
        when(usuarioRepository.findByEmail("ramon@test.com")).thenReturn(Optional.of(usuario));
        when(moradorRepository.findByCasaIdAndUsuarioEmail(1L, "ramon@test.com")).thenReturn(Optional.of(morador));

        String result = republicaTools.registrar_despesa(1L, "Energia", BigDecimal.valueOf(150.0), "2026-07-30", 10L, "INVALID_TYPE", "chave-pix", "ramon@test.com");

        assertTrue(result.contains("Erro"));
        assertTrue(result.contains("Deve ser 'FIXA' ou 'OCASIONAL'"));
        verify(despesaService, never()).cadastrarDespesa(any(), any());
    }

    @Test
    void dividirDespesasComSucesso() {
        when(moradorRepository.findByCasaIdAndUsuarioEmail(1L, "ramon@test.com")).thenReturn(Optional.of(morador));
        
        List<Despesa> despesas = new ArrayList<>();
        Despesa d = new Despesa(casa, "Energia", BigDecimal.valueOf(100.0), LocalDate.parse("2026-07-30"), morador, StatusDespesa.PENDENTE, TipoDespesa.FIXA, "pix");
        d.setId(101L);
        
        DespesaRateio r = new DespesaRateio(d, morador, BigDecimal.valueOf(100.0));
        d.setRateios(List.of(r));
        despesas.add(d);

        when(despesaRepository.findByCasaIdAndExcluidoFalseOrderByVencimentoAsc(1L)).thenReturn(despesas);

        String result = republicaTools.dividir_despesas(1L, "2026-07", "ramon@test.com");

        assertTrue(result.contains("Divisão de Despesas"));
        assertTrue(result.contains("Energia"));
        assertTrue(result.contains("R$ 100,00"));
    }

    @Test
    void dividirDespesasNenhumaNoMes() {
        when(moradorRepository.findByCasaIdAndUsuarioEmail(1L, "ramon@test.com")).thenReturn(Optional.of(morador));
        when(despesaRepository.findByCasaIdAndExcluidoFalseOrderByVencimentoAsc(1L)).thenReturn(new ArrayList<>());

        String result = republicaTools.dividir_despesas(1L, "2026-07", "ramon@test.com");

        assertTrue(result.contains("Nenhuma despesa localizada"));
    }

    @Test
    void saldoMoradorComSucesso() {
        when(moradorRepository.findById(10L)).thenReturn(Optional.of(morador));
        
        List<DespesaRateio> rateios = new ArrayList<>();
        Despesa d = new Despesa(casa, "Internet", BigDecimal.valueOf(90.0), LocalDate.parse("2026-07-15"), morador, StatusDespesa.PENDENTE, TipoDespesa.FIXA, "pix");
        DespesaRateio r = new DespesaRateio(d, morador, BigDecimal.valueOf(45.0));
        rateios.add(r);

        when(despesaRateioRepository.findByMoradorId(10L)).thenReturn(rateios);

        String result = republicaTools.saldo_morador(10L, null, null);

        assertTrue(result.contains("Saldo Devedor"));
        assertTrue(result.contains("Internet"));
        assertTrue(result.contains("R$ 45,00"));
    }

    @Test
    void saldoMoradorEmDia() {
        when(moradorRepository.findById(10L)).thenReturn(Optional.of(morador));
        when(despesaRateioRepository.findByMoradorId(10L)).thenReturn(new ArrayList<>());

        String result = republicaTools.saldo_morador(10L, null, null);

        assertTrue(result.contains("contas em dia"));
    }

    @Test
    void notificarMoradoresComSucesso() {
        when(usuarioRepository.findByEmail("ramon@test.com")).thenReturn(Optional.of(usuario));
        when(moradorRepository.findByCasaIdAndUsuarioEmail(1L, "ramon@test.com")).thenReturn(Optional.of(morador));
        when(moradorRepository.findByCasaId(1L)).thenReturn(List.of(morador));

        String result = republicaTools.notificar_moradores(1L, "Aviso Importante", "Mensagem de teste", "ramon@test.com");

        assertTrue(result.contains("Sucesso!"));
        assertTrue(result.contains("Aviso Importante"));
        verify(notificacaoService, times(1)).criarNotificacao(any(), eq("Aviso Importante"), eq("Mensagem de teste"), eq(TipoNotificacao.AVISO));
    }

    @Test
    void extratoCasaComSucesso() {
        when(casaRepository.findById(1L)).thenReturn(Optional.of(casa));
        
        List<Despesa> despesas = new ArrayList<>();
        Despesa d = new Despesa(casa, "Água", BigDecimal.valueOf(80.0), LocalDate.parse("2026-07-20"), morador, StatusDespesa.PENDENTE, TipoDespesa.FIXA, "pix");
        d.setId(102L);
        
        DespesaRateio r = new DespesaRateio(d, morador, BigDecimal.valueOf(80.0));
        d.setRateios(List.of(r));
        despesas.add(d);

        when(despesaRepository.findByCasaIdAndExcluidoFalseOrderByVencimentoAsc(1L)).thenReturn(despesas);

        String result = republicaTools.extrato_casa(1L);


        assertTrue(result.contains("EXTRATO FINANCEIRO"));
        assertTrue(result.contains("Água"));
        assertTrue(result.contains("R$ 80,00"));
    }
}
