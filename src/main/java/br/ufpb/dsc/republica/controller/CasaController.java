package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Casa;
import br.ufpb.dsc.republica.domain.Despesa;
import br.ufpb.dsc.republica.domain.Morador;
import br.ufpb.dsc.republica.domain.Tarefa;
import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.*;
import br.ufpb.dsc.republica.service.CasaService;
import br.ufpb.dsc.republica.service.DespesaService;
import br.ufpb.dsc.republica.service.TarefaService;
import br.ufpb.dsc.republica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para gerenciar repúblicas/casas.
 */
@RestController
@RequestMapping("/api/casas")
public class CasaController {

    private final CasaService casaService;
    private final DespesaService despesaService;
    private final TarefaService tarefaService;
    private final UsuarioService usuarioService;

    public CasaController(CasaService casaService, DespesaService despesaService,
                          TarefaService tarefaService, UsuarioService usuarioService) {
        this.casaService = casaService;
        this.despesaService = despesaService;
        this.tarefaService = tarefaService;
        this.usuarioService = usuarioService;
    }

    /**
     * Cria uma nova casa/república e define o criador como ADMINISTRADOR.
     */
    @PostMapping
    public ResponseEntity<CasaDto> criarCasa(@Valid @RequestBody CasaForm form, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        Casa casa = casaService.criarCasa(form, principal.getName());
        return ResponseEntity.ok(new CasaDto(casa.getId(), casa.getNome(), casa.getEndereco(), casa.getCriadoEm()));
    }

    /**
     * Retorna todos os detalhes de uma casa (moradores, despesas, tarefas).
     */
    @GetMapping("/{id}")
    public ResponseEntity<DetalhesCasaResponse> exibirDetalhes(@PathVariable("id") Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Usuario usuario = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        Casa casa = casaService.buscarPorId(id);
        Morador moradorLogado = casaService.buscarMoradorPorUsuarioECasa(casa.getId(), usuario.getId());

        List<Morador> moradores = casaService.buscarMoradores(id);
        List<Despesa> despesas = despesaService.buscarDespesasPorCasa(id);
        List<Tarefa> tarefas = tarefaService.buscarTarefasPorCasa(id);

        CasaDto casaDto = new CasaDto(casa.getId(), casa.getNome(), casa.getEndereco(), casa.getCriadoEm());
        MoradorDto moradorLogadoDto = toMoradorDto(moradorLogado);

        List<MoradorDto> moradoresDto = moradores.stream()
                .map(this::toMoradorDto)
                .toList();

        List<DespesaDto> despesasDto = despesas.stream()
                .map(d -> new DespesaDto(
                        d.getId(),
                        d.getDescricao(),
                        d.getValorTotal(),
                        d.getVencimento(),
                        d.getStatus(),
                        toMoradorDto(d.getResponsavel()),
                        d.getRateios().stream()
                                .map(r -> new DespesaRateioDto(
                                        r.getId(),
                                        toMoradorDto(r.getMorador()),
                                        r.getValorDevido(),
                                        r.getUltimoPagamento() != null ? r.getUltimoPagamento().getStatus() : null,
                                        r.getUltimoPagamento() != null ? r.getUltimoPagamento().getId() : null,
                                        r.getUltimoPagamento() != null ? r.getUltimoPagamento().getComprovante() : null,
                                        r.getUltimoPagamento() != null ? r.getUltimoPagamento().getDataPagamento() : null
                                ))
                                .toList()
                ))
                .toList();

        List<TarefaDto> tarefasDto = tarefas.stream()
                .map(t -> new TarefaDto(
                        t.getId(),
                        t.getDescricao(),
                        t.getStatus(),
                        toMoradorDto(t.getResponsavel())
                ))
                .toList();

        return ResponseEntity.ok(new DetalhesCasaResponse(
                casaDto,
                moradorLogadoDto,
                moradoresDto,
                despesasDto,
                tarefasDto
        ));
    }

    /**
     * Adiciona/convida um novo morador à casa pelo e-mail.
     */
    @PostMapping("/{id}/moradores")
    public ResponseEntity<MoradorDto> convidarMorador(@PathVariable("id") Long id,
                                                      @RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("O e-mail do morador é obrigatório.");
        }
        Morador morador = casaService.adicionarMorador(id, email);
        return ResponseEntity.ok(toMoradorDto(morador));
    }

    private MoradorDto toMoradorDto(Morador m) {
        if (m == null) {
            return null;
        }
        return new MoradorDto(
                m.getId(),
                m.getUsuario().getNome(),
                m.getUsuario().getEmail(),
                m.getPapel()
        );
    }
}


