package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.UsuarioForm;
import br.ufpb.dsc.republica.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioForm form;

    @BeforeEach
    void setUp() {
        form = new UsuarioForm("Teste", "teste@email.com", "senha123");
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
    void cadastrarDeveCriarNovoUsuarioComSenhaCriptografada() {
        when(usuarioRepository.existsByEmail(form.email())).thenReturn(false);
        when(passwordEncoder.encode(form.senha())).thenReturn("senhaCriptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario usuarioCriado = usuarioService.cadastrar(form);

        assertNotNull(usuarioCriado);
        assertEquals("Teste", usuarioCriado.getNome());
        assertEquals("teste@email.com", usuarioCriado.getEmail());
        assertEquals("senhaCriptografada", usuarioCriado.getSenha());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void cadastrarDeveLancarExcecaoSeEmailJaExistir() {
        when(usuarioRepository.existsByEmail(form.email())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.cadastrar(form));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
