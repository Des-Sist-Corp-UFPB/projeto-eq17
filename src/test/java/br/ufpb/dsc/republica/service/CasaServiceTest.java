package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.Casa;
import br.ufpb.dsc.republica.domain.Morador;
import br.ufpb.dsc.republica.domain.PapelMorador;
import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.CasaForm;
import br.ufpb.dsc.republica.repository.CasaRepository;
import br.ufpb.dsc.republica.repository.MoradorRepository;
import br.ufpb.dsc.republica.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CasaServiceTest {

    @Mock
    private CasaRepository casaRepository;

    @Mock
    private MoradorRepository moradorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private CasaService casaService;

    private Usuario usuario;
    private Casa casa;
    private CasaForm casaForm;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("Morador Teste", "morador@email.com", "senha");
        usuario.setId(1L);
        casa = new Casa("Casa Principal", "Rua das Flores, 123");
        casa.setId(10L);
        casaForm = new CasaForm("Casa Principal", "Rua das Flores, 123");
    }

    @Test
    void buscarCasasPorUsuarioDeveRetornarListaDeCasas() {
        when(casaRepository.findCasasByUsuarioId(1L)).thenReturn(List.of(casa));
        List<Casa> result = casaService.buscarCasasPorUsuario(1L);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Casa Principal", result.get(0).getNome());
        verify(casaRepository, times(1)).findCasasByUsuarioId(1L);
    }

    @Test
    void buscarPorIdDeveRetornarCasaSeExistir() {
        when(casaRepository.findById(10L)).thenReturn(Optional.of(casa));
        Casa result = casaService.buscarPorId(10L);
        assertNotNull(result);
        assertEquals("Casa Principal", result.getNome());
    }

    @Test
    void buscarPorIdDeveLancarExcecaoSeNaoExistir() {
        when(casaRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> casaService.buscarPorId(10L));
    }

    @Test
    void criarCasaDeveRetornarCasaSalvaECriarAdmin() {
        when(usuarioRepository.findByEmail("morador@email.com")).thenReturn(Optional.of(usuario));
        when(casaRepository.save(any(Casa.class))).thenReturn(casa);
        when(moradorRepository.save(any(Morador.class))).thenAnswer(i -> i.getArgument(0));

        Casa result = casaService.criarCasa(casaForm, "morador@email.com");

        assertNotNull(result);
        assertEquals("Casa Principal", result.getNome());
        verify(casaRepository, times(1)).save(any(Casa.class));
        verify(moradorRepository, times(1)).save(any(Morador.class));
        verify(auditoriaService, times(1)).registrar(eq(usuario), eq("CRIACAO_CASA"), anyString(), eq("Casa"), eq(10L));
    }

    @Test
    void criarCasaDeveLancarExcecaoSeUsuarioNaoExistir() {
        when(usuarioRepository.findByEmail("morador@email.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> casaService.criarCasa(casaForm, "morador@email.com"));
    }

    @Test
    void adicionarMoradorDeveSalvarMoradorSeValido() {
        when(casaRepository.findById(10L)).thenReturn(Optional.of(casa));
        when(usuarioRepository.findByEmail("morador@email.com")).thenReturn(Optional.of(usuario));
        when(moradorRepository.existsByCasaIdAndUsuarioId(10L, 1L)).thenReturn(false);
        when(moradorRepository.save(any(Morador.class))).thenAnswer(i -> i.getArgument(0));

        Morador result = casaService.adicionarMorador(10L, "morador@email.com");

        assertNotNull(result);
        assertEquals(casa, result.getCasa());
        assertEquals(usuario, result.getUsuario());
        assertEquals(PapelMorador.MORADOR, result.getPapel());
        verify(moradorRepository, times(1)).save(any(Morador.class));
        verify(auditoriaService, times(1)).registrarAcaoUsuarioLogado(eq("ENTRADA_MORADORES"), anyString(), eq("Morador"), any());
    }

    @org.junit.jupiter.api.Disabled("Diminuindo cobertura para 90% a pedido do usuario")
    @Test
    void adicionarMoradorDeveLancarExcecaoSeJaForMorador() {
        when(casaRepository.findById(10L)).thenReturn(Optional.of(casa));
        when(usuarioRepository.findByEmail("morador@email.com")).thenReturn(Optional.of(usuario));
        when(moradorRepository.existsByCasaIdAndUsuarioId(10L, 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> casaService.adicionarMorador(10L, "morador@email.com"));
    }

    @Test
    void buscarMoradoresDeveRetornarListaDeMoradores() {
        Morador morador = new Morador(usuario, casa, PapelMorador.MORADOR);
        when(moradorRepository.findByCasaId(10L)).thenReturn(List.of(morador));
        List<Morador> result = casaService.buscarMoradores(10L);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(usuario, result.get(0).getUsuario());
    }

    @Test
    void buscarMoradorPorUsuarioECasaDeveRetornarMoradorSeExistir() {
        Morador morador = new Morador(usuario, casa, PapelMorador.MORADOR);
        when(moradorRepository.findByCasaIdAndUsuarioId(10L, 1L)).thenReturn(Optional.of(morador));
        Morador result = casaService.buscarMoradorPorUsuarioECasa(10L, 1L);
        assertNotNull(result);
        assertEquals(morador, result);
    }

    @org.junit.jupiter.api.Disabled("Diminuindo cobertura para 90% a pedido do usuario")
    @Test
    void buscarMoradorPorUsuarioECasaDeveLancarExcecaoSeNaoForMorador() {
        when(moradorRepository.findByCasaIdAndUsuarioId(10L, 1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> casaService.buscarMoradorPorUsuarioECasa(10L, 1L));
    }
}
