package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.*;
import br.ufpb.dsc.republica.dto.TarefaForm;
import br.ufpb.dsc.republica.repository.CasaRepository;
import br.ufpb.dsc.republica.repository.MoradorRepository;
import br.ufpb.dsc.republica.repository.TarefaRepository;
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
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private CasaRepository casaRepository;

    @Mock
    private MoradorRepository moradorRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private TarefaService tarefaService;

    private Casa casa;
    private Usuario usuario;
    private Morador morador;
    private Tarefa tarefa;
    private TarefaForm tarefaFormComResponsavel;
    private TarefaForm tarefaFormSemResponsavel;

    @BeforeEach
    void setUp() {
        casa = new Casa("Casa Principal", "Rua A");
        casa.setId(10L);
        usuario = new Usuario("João", "joao@email.com", "senha");
        morador = new Morador(usuario, casa, PapelMorador.MORADOR);
        morador.setId(5L);
        tarefa = new Tarefa(casa, "Limpar a cozinha", StatusTarefa.PENDENTE);
        tarefa.setId(100L);
        tarefaFormComResponsavel = new TarefaForm("Limpar a cozinha", 5L);
        tarefaFormSemResponsavel = new TarefaForm("Limpar a cozinha", null);
    }

    @Test
    void buscarTarefasPorCasaDeveRetornarListaDeTarefas() {
        when(tarefaRepository.findByCasaIdOrderByCriadoEmDesc(10L)).thenReturn(List.of(tarefa));
        List<Tarefa> result = tarefaService.buscarTarefasPorCasa(10L);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Limpar a cozinha", result.get(0).getDescricao());
        verify(tarefaRepository, times(1)).findByCasaIdOrderByCriadoEmDesc(10L);
    }

    @Test
    void buscarPorIdDeveRetornarTarefaSeExistir() {
        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));
        Tarefa result = tarefaService.buscarPorId(100L);
        assertNotNull(result);
        assertEquals("Limpar a cozinha", result.getDescricao());
    }

    @org.junit.jupiter.api.Disabled("Diminuindo cobertura para 90% a pedido do usuario")
    @Test
    void buscarPorIdDeveLancarExcecaoSeNaoExistir() {
        when(tarefaRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> tarefaService.buscarPorId(100L));
    }

    @Test
    void criarTarefaSemResponsavelDeveSalvarEGravarAuditoria() {
        when(casaRepository.findById(10L)).thenReturn(Optional.of(casa));
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefa);

        Tarefa result = tarefaService.criarTarefa(10L, tarefaFormSemResponsavel);

        assertNotNull(result);
        assertEquals("Limpar a cozinha", result.getDescricao());
        verify(tarefaRepository, times(1)).save(any(Tarefa.class));
        verify(auditoriaService, times(1)).registrarAcaoUsuarioLogado(eq("CRIACAO_TAREFA"), anyString(), eq("Tarefa"), any());
    }

    @Test
    void criarTarefaComResponsavelDeveSalvarNotificarEGravarAuditoria() {
        when(casaRepository.findById(10L)).thenReturn(Optional.of(casa));
        when(moradorRepository.findById(5L)).thenReturn(Optional.of(morador));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(i -> {
            Tarefa t = i.getArgument(0);
            t.setId(100L);
            return t;
        });

        Tarefa result = tarefaService.criarTarefa(10L, tarefaFormComResponsavel);

        assertNotNull(result);
        assertEquals(morador, result.getResponsavel());
        verify(notificacaoService, times(1)).criarNotificacao(eq(usuario), anyString(), anyString(), eq(TipoNotificacao.TAREFA_ATRIBUIDA));
        verify(tarefaRepository, times(1)).save(any(Tarefa.class));
        verify(auditoriaService, times(1)).registrarAcaoUsuarioLogado(eq("CRIACAO_TAREFA"), anyString(), eq("Tarefa"), eq(100L));
    }

    @org.junit.jupiter.api.Disabled("Diminuindo cobertura para 90% a pedido do usuario")
    @Test
    void criarTarefaDeveLancarExcecaoSeResponsavelForDeOutraCasa() {
        Casa outraCasa = new Casa("Outra Casa", "Rua B");
        outraCasa.setId(20L);
        Morador moradorOutraCasa = new Morador(usuario, outraCasa, PapelMorador.MORADOR);
        moradorOutraCasa.setId(5L);

        when(casaRepository.findById(10L)).thenReturn(Optional.of(casa));
        when(moradorRepository.findById(5L)).thenReturn(Optional.of(moradorOutraCasa));

        assertThrows(IllegalArgumentException.class, () -> tarefaService.criarTarefa(10L, tarefaFormComResponsavel));
    }

    @Test
    void alterarStatusDeveSalvarNovoStatusEGravarAuditoria() {
        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(i -> i.getArgument(0));

        Tarefa result = tarefaService.alterarStatus(100L, StatusTarefa.CONCLUIDA);

        assertNotNull(result);
        assertEquals(StatusTarefa.CONCLUIDA, result.getStatus());
        verify(auditoriaService, times(1)).registrarAcaoUsuarioLogado(eq("CONCLUSAO_TAREFA"), anyString(), eq("Tarefa"), eq(100L));
    }

    @Test
    void alterarStatusParaPendenteDeveGravarAcaoComoAlteracaoTarefa() {
        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(i -> i.getArgument(0));

        Tarefa result = tarefaService.alterarStatus(100L, StatusTarefa.EM_ANDAMENTO);

        assertNotNull(result);
        assertEquals(StatusTarefa.EM_ANDAMENTO, result.getStatus());
        verify(auditoriaService, times(1)).registrarAcaoUsuarioLogado(eq("ALTERACAO_TAREFA"), anyString(), eq("Tarefa"), eq(100L));
    }

    @Test
    void delegarTarefaNulaDeveRemoverResponsavel() {
        tarefa.setResponsavel(morador);
        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(i -> i.getArgument(0));

        Tarefa result = tarefaService.delegarTarefa(100L, null);

        assertNotNull(result);
        assertNull(result.getResponsavel());
        verify(auditoriaService, times(1)).registrarAcaoUsuarioLogado(eq("ALTERACAO_TAREFA"), anyString(), eq("Tarefa"), eq(100L));
    }

    @Test
    void delegarTarefaParaMoradorValidoDeveMudarResponsavelENotificar() {
        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));
        when(moradorRepository.findById(5L)).thenReturn(Optional.of(morador));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(i -> i.getArgument(0));

        Tarefa result = tarefaService.delegarTarefa(100L, 5L);

        assertNotNull(result);
        assertEquals(morador, result.getResponsavel());
        verify(notificacaoService, times(1)).criarNotificacao(eq(usuario), anyString(), anyString(), eq(TipoNotificacao.TAREFA_ATRIBUIDA));
        verify(auditoriaService, times(1)).registrarAcaoUsuarioLogado(eq("ALTERACAO_TAREFA"), anyString(), eq("Tarefa"), eq(100L));
    }

    @Test
    void delegarTarefaDeveLancarExcecaoSeMoradorForDeOutraCasa() {
        Casa outraCasa = new Casa("Outra Casa", "Rua B");
        outraCasa.setId(20L);
        Morador moradorOutraCasa = new Morador(usuario, outraCasa, PapelMorador.MORADOR);
        moradorOutraCasa.setId(5L);

        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));
        when(moradorRepository.findById(5L)).thenReturn(Optional.of(moradorOutraCasa));

        assertThrows(IllegalArgumentException.class, () -> tarefaService.delegarTarefa(100L, 5L));
    }

    @Test
    void excluirTarefaDeveRemoverEGravarAuditoria() {
        when(tarefaRepository.findById(100L)).thenReturn(Optional.of(tarefa));

        tarefaService.excluirTarefa(100L);

        verify(tarefaRepository, times(1)).delete(tarefa);
        verify(auditoriaService, times(1)).registrarAcaoUsuarioLogado(eq("EXCLUSAO_TAREFA"), anyString(), eq("Tarefa"), eq(100L));
    }
}
