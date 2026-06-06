package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.Casa;
import br.ufpb.dsc.republica.domain.Morador;
import br.ufpb.dsc.republica.domain.PapelMorador;
import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.CasaForm;
import br.ufpb.dsc.republica.repository.CasaRepository;
import br.ufpb.dsc.republica.repository.MoradorRepository;
import br.ufpb.dsc.republica.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CasaService {

    private final CasaRepository casaRepository;
    private final MoradorRepository moradorRepository;
    private final UsuarioRepository usuarioRepository;

    public CasaService(CasaRepository casaRepository, MoradorRepository moradorRepository, UsuarioRepository usuarioRepository) {
        this.casaRepository = casaRepository;
        this.moradorRepository = moradorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Casa> buscarCasasPorUsuario(Long usuarioId) {
        return casaRepository.findCasasByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Casa buscarPorId(Long id) {
        return casaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Casa não encontrada com o ID: " + id));
    }

    public Casa criarCasa(CasaForm form, String emailCriador) {
        Usuario criador = usuarioRepository.findByEmail(emailCriador)
                .orElseThrow(() -> new IllegalArgumentException("Usuário criador não encontrado."));

        Casa novaCasa = new Casa(form.nome(), form.endereco());
        novaCasa = casaRepository.save(novaCasa);

        // O criador é automaticamente associado como ADMINISTRADOR
        Morador admin = new Morador(criador, novaCasa, PapelMorador.ADMINISTRADOR);
        moradorRepository.save(admin);

        return novaCasa;
    }

    public Morador adicionarMorador(Long casaId, String emailMorador) {
        Casa casa = buscarPorId(casaId);
        Usuario usuario = usuarioRepository.findByEmail(emailMorador)
                .orElseThrow(() -> new IllegalArgumentException("Nenhum usuário cadastrado com o e-mail: " + emailMorador));

        if (moradorRepository.existsByCasaIdAndUsuarioId(casaId, usuario.getId())) {
            throw new IllegalArgumentException("Este usuário já é morador desta casa.");
        }

        Morador morador = new Morador(usuario, casa, PapelMorador.MORADOR);
        return moradorRepository.save(morador);
    }

    @Transactional(readOnly = true)
    public List<Morador> buscarMoradores(Long casaId) {
        return moradorRepository.findByCasaId(casaId);
    }

    @Transactional(readOnly = true)
    public Morador buscarMoradorPorUsuarioECasa(Long casaId, Long usuarioId) {
        return moradorRepository.findByCasaIdAndUsuarioId(casaId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não é morador desta casa."));
    }
}

