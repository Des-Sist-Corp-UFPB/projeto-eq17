package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Casa;
import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.CasaDto;
import br.ufpb.dsc.republica.dto.DashboardResponse;
import br.ufpb.dsc.republica.dto.UsuarioDto;
import br.ufpb.dsc.republica.service.CasaService;
import br.ufpb.dsc.republica.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * Controller REST do dashboard do usuário.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CasaService casaService;
    private final UsuarioService usuarioService;

    public DashboardController(CasaService casaService, UsuarioService usuarioService) {
        this.casaService = casaService;
        this.usuarioService = usuarioService;
    }

    /**
     * Retorna os dados do dashboard do usuário logado (perfil e suas repúblicas).
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> exibirDashboard(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Usuario usuario = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        List<Casa> casas = casaService.buscarCasasPorUsuario(usuario.getId());

        UsuarioDto usuarioDto = new UsuarioDto(usuario.getId(), usuario.getNome(), usuario.getEmail());
        List<CasaDto> casasDto = casas.stream()
                .map(c -> new CasaDto(c.getId(), c.getNome(), c.getEndereco(), c.getCriadoEm()))
                .toList();

        return ResponseEntity.ok(new DashboardResponse(usuarioDto, casasDto));
    }
}


