package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.*;
import br.ufpb.dsc.republica.dto.LgpdExportDto;
import br.ufpb.dsc.republica.dto.UsuarioForm;
import br.ufpb.dsc.republica.repository.DespesaRateioRepository;
import br.ufpb.dsc.republica.repository.MoradorRepository;
import br.ufpb.dsc.republica.repository.TarefaRepository;
import br.ufpb.dsc.republica.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MoradorRepository moradorRepository;

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private DespesaRateioRepository despesaRateioRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioForm form;

    @BeforeEach
    void setUp() {
        form = new UsuarioForm("Teste", "teste@email.com", "senha123", true, "1.0");
    }

    @Test
    void buscarPorEmailDeveRetornarUsuarioSeExistir() {
        Usuario usuario = new Usuario("Teste", "teste@email.com", "senhaHash");
        when(usuarioRepository.findByEmail("teste@email.com")).thenReturn(Optional.of(usuario));

        Optional<Usuario> result = usuarioService.buscarPorEmail("teste@email.com");

        assertTrue(result.isPresent());
        assertEquals("Teste", result.get().getNome());
        verify(usuarioRepository, times(1)).findByEmail("teste@email.com");
    }

    @Test
    void cadastrarDeveCriarNovoUsuarioComSenhaCriptografadaEAceiteLGPD() {
        when(usuarioRepository.existsByEmail(form.email())).thenReturn(false);
        when(passwordEncoder.encode(form.senha())).thenReturn("senhaCriptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario usuarioCriado = usuarioService.cadastrar(form);

        assertNotNull(usuarioCriado);
        assertEquals("Teste", usuarioCriado.getNome());
        assertEquals("teste@email.com", usuarioCriado.getEmail());
        assertEquals("senhaCriptografada", usuarioCriado.getSenha());
        assertTrue(usuarioCriado.getAceitouTermosLgpd());
        assertNotNull(usuarioCriado.getDataAceiteLgpd());
        assertEquals("1.0", usuarioCriado.getVersaoTermoLgpd());

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(auditoriaService, times(1)).registrar(any(Usuario.class), eq("CADASTRO"), anyString(), eq("Usuario"), any());
    }

    @Test
    void cadastrarDeveLancarExcecaoSeEmailJaExistir() {
        when(usuarioRepository.existsByEmail(form.email())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.cadastrar(form));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void cadastrarDeveLancarExcecaoSeNaoAceitarTermosLgpd() {
        UsuarioForm formSemAceite = new UsuarioForm("Teste", "teste@email.com", "senha123", false, "1.0");
        when(usuarioRepository.existsByEmail(formSemAceite.email())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.cadastrar(formSemAceite));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void possuiPendenciasFinanceirasDeveRetornarTrueSeHouverPagamentoNaoConfirmado() {
        Long usuarioId = 1L;
        Morador morador = new Morador();
        morador.setId(10L);
        
        Despesa despesa = new Despesa();
        despesa.setExcluido(false);
        despesa.setDescricao("Energia");
        
        DespesaRateio rateio = new DespesaRateio(despesa, morador, BigDecimal.TEN);
        Pagamento pagamento = new Pagamento(rateio, StatusPagamento.PENDENTE);
        rateio.getPagamentos().add(pagamento);

        when(moradorRepository.findByUsuarioId(usuarioId)).thenReturn(List.of(morador));
        when(despesaRateioRepository.findByMoradorId(10L)).thenReturn(List.of(rateio));

        boolean possuiPendencias = usuarioService.possuiPendenciasFinanceiras(usuarioId);

        assertTrue(possuiPendencias);
    }

    @Test
    void possuiPendenciasFinanceirasDeveRetornarFalseSeTodosPagamentosConfirmados() {
        Long usuarioId = 1L;
        Morador morador = new Morador();
        morador.setId(10L);
        
        Despesa despesa = new Despesa();
        despesa.setExcluido(false);
        
        DespesaRateio rateio = new DespesaRateio(despesa, morador, BigDecimal.TEN);
        Pagamento pagamento = new Pagamento(rateio, StatusPagamento.CONFIRMADO);
        rateio.getPagamentos().add(pagamento);

        when(moradorRepository.findByUsuarioId(usuarioId)).thenReturn(List.of(morador));
        when(despesaRateioRepository.findByMoradorId(10L)).thenReturn(List.of(rateio));

        boolean possuiPendencias = usuarioService.possuiPendenciasFinanceiras(usuarioId);

        assertFalse(possuiPendencias);
    }

    @Test
    void excluirUsuarioDeveAnonimizarSePossuirPendencias() {
        Long usuarioId = 1L;
        Usuario usuario = new Usuario("João Silva", "joao@email.com", "senhaCripto");
        usuario.setId(usuarioId);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        // Mock possuiPendenciasFinanceiras indiretamente mockando os retornos dos repositórios
        Morador morador = new Morador();
        morador.setId(10L);
        Despesa despesa = new Despesa();
        despesa.setExcluido(false);
        DespesaRateio rateio = new DespesaRateio(despesa, morador, BigDecimal.TEN);
        Pagamento pagamento = new Pagamento(rateio, StatusPagamento.PENDENTE);
        rateio.getPagamentos().add(pagamento);

        when(moradorRepository.findByUsuarioId(usuarioId)).thenReturn(List.of(morador));
        when(despesaRateioRepository.findByMoradorId(10L)).thenReturn(List.of(rateio));
        when(passwordEncoder.encode(anyString())).thenReturn("novaSenhaCripto");

        usuarioService.excluirUsuario(usuarioId);

        assertEquals("Usuario Removido #1", usuario.getNome());
        assertEquals("removido1@sistema.local", usuario.getEmail());
        assertEquals("novaSenhaCripto", usuario.getSenha());
        assertFalse(usuario.getAceitouTermosLgpd());

        verify(usuarioRepository, times(1)).save(usuario);
        verify(auditoriaService, times(1)).registrar(eq(usuario), eq("SOLICITACAO_EXCLUSAO"), anyString(), eq("Usuario"), eq(usuarioId));
    }

    @Test
    void excluirUsuarioDeveDeletarFisicamenteSeNaoPossuirPendencias() {
        Long usuarioId = 1L;
        Usuario usuario = new Usuario("João Silva", "joao@email.com", "senhaCripto");
        usuario.setId(usuarioId);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(moradorRepository.findByUsuarioId(usuarioId)).thenReturn(Collections.emptyList());

        usuarioService.excluirUsuario(usuarioId);

        verify(usuarioRepository, times(1)).delete(usuario);
        verify(auditoriaService, times(1)).registrar(isNull(), eq("SOLICITACAO_EXCLUSAO"), anyString(), eq("Usuario"), eq(usuarioId));
    }

    @Test
    void exportarDadosDeveRetornarDtoCompleto() {
        Long usuarioId = 1L;
        Usuario usuario = new Usuario("João Silva", "joao@email.com", "senhaCripto");
        usuario.setId(usuarioId);
        usuario.setAceitouTermosLgpd(true);
        usuario.setVersaoTermoLgpd("1.0");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(moradorRepository.findByUsuarioId(usuarioId)).thenReturn(Collections.emptyList());
        when(auditoriaService.buscarPorUsuario(usuarioId)).thenReturn(Collections.emptyList());

        LgpdExportDto exportDto = usuarioService.exportarDados(usuarioId);

        assertNotNull(exportDto);
        assertEquals("João Silva", exportDto.cadastral().nome());
        assertEquals("joao@email.com", exportDto.cadastral().email());
        assertTrue(exportDto.cadastral().aceitouTermosLgpd());
        assertEquals("1.0", exportDto.cadastral().versaoTermoLgpd());

        verify(auditoriaService, times(1)).registrar(eq(usuario), eq("EXPORTACAO_DADOS"), anyString(), eq("Usuario"), eq(usuarioId));
    }
}
