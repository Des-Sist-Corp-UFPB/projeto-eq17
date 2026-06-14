package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.Auditoria;
import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.repository.AuditoriaRepository;
import br.ufpb.dsc.republica.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
@Transactional
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HttpServletRequest request;

    public AuditoriaService(AuditoriaRepository auditoriaRepository,
                            UsuarioRepository usuarioRepository,
                            HttpServletRequest request) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.request = request;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Auditoria registrar(Usuario usuario, String acao, String descricao, String entidadeAfetada, Long entidadeId) {
        String ip = obterIpCliente();
        Auditoria auditoria = new Auditoria(usuario, acao, descricao, ip, entidadeAfetada, entidadeId);
        return auditoriaRepository.save(auditoria);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Auditoria registrarAcaoUsuarioLogado(String acao, String descricao, String entidadeAfetada, Long entidadeId) {
        Usuario usuario = obterUsuarioLogado();
        return registrar(usuario, acao, descricao, entidadeAfetada, entidadeId);
    }

    @Transactional(readOnly = true)
    public List<Auditoria> buscarPorUsuario(Long usuarioId) {
        return auditoriaRepository.findByUsuarioIdOrderByDataHoraDesc(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Auditoria> buscarTodas() {
        return auditoriaRepository.findAll();
    }

    private String obterIpCliente() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest req = attributes.getRequest();
                String ip = req.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = req.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            // Ignora se estiver fora de uma requisição HTTP
        }
        return "127.0.0.1";
    }

    private Usuario obterUsuarioLogado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                String email = authentication.getName();
                return usuarioRepository.findByEmail(email).orElse(null);
            }
        } catch (Exception e) {
            // Ignora
        }
        return null;
    }
}
