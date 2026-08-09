package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.Auditoria;
import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.repository.AuditoriaRepository;
import br.ufpb.dsc.republica.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuditoriaService auditoriaService;

    private Usuario usuario;
    private Auditoria auditoria;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("João", "joao@email.com", "senha");
        usuario.setId(1L);
        auditoria = new Auditoria(usuario, "CADASTRO", "Usuário cadastrado", "127.0.0.1", "Usuario", 1L);
        auditoria.setId(10L);
    }

    @Test
    void registrarDeveSalvarLogComIpEDefaults() {
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));

        Auditoria result = auditoriaService.registrar(usuario, "CADASTRO", "Usuário cadastrado", "Usuario", 1L);

        assertNotNull(result);
        assertEquals("CADASTRO", result.getAcao());
        assertEquals("Usuário cadastrado", result.getDescricao());
        assertEquals(usuario, result.getUsuario());
        // Fora de requisição HTTP, deve cair no catch e retornar o fallback IP "127.0.0.1"
        assertEquals("127.0.0.1", result.getEnderecoIp());
        verify(auditoriaRepository, times(1)).save(any(Auditoria.class));
    }

    @Test
    void registrarAcaoUsuarioLogadoSemContextoDeveRegistrarLogComUsuarioEIpNulos() {
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));

        Auditoria result = auditoriaService.registrarAcaoUsuarioLogado("ACAO_LOGADA", "Ação de teste", "Entidade", 100L);

        assertNotNull(result);
        assertEquals("ACAO_LOGADA", result.getAcao());
        assertNull(result.getUsuario()); // Sem autenticação no contexto
        assertEquals("127.0.0.1", result.getEnderecoIp()); // Sem contexto de requisição
        verify(auditoriaRepository, times(1)).save(any(Auditoria.class));
    }

    @Test
    void buscarPorUsuarioDeveRetornarLogsOrdenados() {
        when(auditoriaRepository.findByUsuarioIdOrderByDataHoraDesc(1L)).thenReturn(List.of(auditoria));

        List<Auditoria> result = auditoriaService.buscarPorUsuario(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CADASTRO", result.get(0).getAcao());
        verify(auditoriaRepository, times(1)).findByUsuarioIdOrderByDataHoraDesc(1L);
    }

    @Test
    void buscarTodasDeveRetornarListaDeLogs() {
        when(auditoriaRepository.findAll()).thenReturn(List.of(auditoria));

        List<Auditoria> result = auditoriaService.buscarTodas();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditoriaRepository, times(1)).findAll();
    }

    @Test
    void registrarAcaoUsuarioLogadoComUsuarioAutenticado() {
        Usuario usuarioMock = new Usuario("Maria", "maria@email.com", "senha");
        usuarioMock.setId(2L);

        org.springframework.security.core.Authentication authentication = mock(org.springframework.security.core.Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("maria@email.com");
        when(authentication.getPrincipal()).thenReturn("maria@email.com");

        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (var mockedSecurity = mockStatic(org.springframework.security.core.context.SecurityContextHolder.class)) {
            mockedSecurity.when(org.springframework.security.core.context.SecurityContextHolder::getContext).thenReturn(securityContext);
            when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuarioMock));
            when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));

            Auditoria result = auditoriaService.registrarAcaoUsuarioLogado("ACAO_LOGADA", "Ação de teste", "Entidade", 100L);

            assertNotNull(result);
            assertEquals(usuarioMock, result.getUsuario());
            assertEquals("127.0.0.1", result.getEnderecoIp());
            verify(usuarioRepository, times(1)).findByEmail("maria@email.com");
        }
    }

    @org.junit.jupiter.api.Disabled("Diminuindo cobertura para 90% a pedido do usuario")
    @Test
    void registrarDeveSalvarLogComIpObtidoDeXForwardedFor() {
        org.springframework.web.context.request.ServletRequestAttributes attributes = mock(org.springframework.web.context.request.ServletRequestAttributes.class);
        jakarta.servlet.http.HttpServletRequest httpServletRequest = mock(jakarta.servlet.http.HttpServletRequest.class);
        
        when(attributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("203.0.113.195");
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));

        try (var mockedContext = mockStatic(org.springframework.web.context.request.RequestContextHolder.class)) {
            mockedContext.when(org.springframework.web.context.request.RequestContextHolder::getRequestAttributes).thenReturn(attributes);

            Auditoria result = auditoriaService.registrar(usuario, "CADASTRO", "Usuário cadastrado", "Usuario", 1L);

            assertNotNull(result);
            assertEquals("203.0.113.195", result.getEnderecoIp());
        }
    }

    @org.junit.jupiter.api.Disabled("Diminuindo cobertura para 90% a pedido do usuario")
    @Test
    void registrarDeveSalvarLogComIpObtidoDeRemoteAddrQuandoHeaderForVazio() {
        org.springframework.web.context.request.ServletRequestAttributes attributes = mock(org.springframework.web.context.request.ServletRequestAttributes.class);
        jakarta.servlet.http.HttpServletRequest httpServletRequest = mock(jakarta.servlet.http.HttpServletRequest.class);
        
        when(attributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("");
        when(httpServletRequest.getRemoteAddr()).thenReturn("198.51.100.1");
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));

        try (var mockedContext = mockStatic(org.springframework.web.context.request.RequestContextHolder.class)) {
            mockedContext.when(org.springframework.web.context.request.RequestContextHolder::getRequestAttributes).thenReturn(attributes);

            Auditoria result = auditoriaService.registrar(usuario, "CADASTRO", "Usuário cadastrado", "Usuario", 1L);

            assertNotNull(result);
            assertEquals("198.51.100.1", result.getEnderecoIp());
        }
    }
}
