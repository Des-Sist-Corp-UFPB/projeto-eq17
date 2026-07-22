package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Casa;
import br.ufpb.dsc.republica.domain.Morador;
import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.ChatRequestDto;
import br.ufpb.dsc.republica.dto.ChatResponseDto;
import br.ufpb.dsc.republica.repository.MoradorRepository;
import br.ufpb.dsc.republica.repository.UsuarioRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;
    private final UsuarioRepository usuarioRepository;
    private final MoradorRepository moradorRepository;

    public ChatController(ChatClient.Builder chatClientBuilder,
                          br.ufpb.dsc.republica.service.RepublicaTools republicaTools,
                          UsuarioRepository usuarioRepository,
                          MoradorRepository moradorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.moradorRepository = moradorRepository;
        
        // Constrói o ChatClient injetando o service de ferramentas
        this.chatClient = chatClientBuilder
                .defaultTools(republicaTools)
                .build();
    }


    @PostMapping
    public ResponseEntity<ChatResponseDto> conversar(@RequestBody ChatRequestDto request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String emailUsuario = principal.getName();
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        List<Morador> moradores = moradorRepository.findByUsuarioId(usuario.getId());
        Casa casa = null;
        Morador morador = null;
        if (!moradores.isEmpty()) {
            morador = moradores.get(0);
            casa = morador.getCasa();
        }

        // Constrói o prompt do sistema para injetar contexto sobre quem está logado
        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("Você é o HomeHub Assistant, um assistente inteligente especializado em gerenciamento de repúblicas universitárias.\n")
                .append("Você tem permissão para ajudar o morador a realizar ações no sistema.\n")
                .append("O usuário logado que está conversando com você é: ").append(usuario.getNome()).append(" (E-mail: ").append(usuario.getEmail()).append(").\n");

        if (casa != null) {
            systemPrompt.append("Ele reside na república/casa: ").append(casa.getNome()).append(" (ID da Casa: ").append(casa.getId()).append(").\n")
                    .append("O ID do morador correspondente é: ").append(morador.getId()).append(".\n")
                    .append("SEMPRE que invocar ferramentas de ação (como registrar_despesa ou notificar_moradores), preencha o parâmetro 'usuarioEmail' com o e-mail do usuário logado ('").append(usuario.getEmail()).append("') ")
                    .append("e o 'casaId' com o ID da casa dele (").append(casa.getId()).append(").\n")
                    .append("Para a despesa, se for registrar despesa, passe também o responsavelId correto (geralmente o ID do morador logado se ele for pagar, ou de outro morador). ")
                    .append("Caso o usuário não especifique o responsável, use o ID do morador logado (").append(morador.getId()).append(").\n");
        } else {
            systemPrompt.append("Aviso: O usuário não está cadastrado em nenhuma casa/república. ")
                    .append("Oriente-o a criar ou entrar em uma república no sistema antes de poder registrar despesas ou enviar avisos.\n");
        }

        systemPrompt.append("\nSeja amigável, direto e ajude respondendo em formato amigável (Markdown).");

        try {
            // Executa a chamada do ChatClient usando as tools
            String respostaText = chatClient.prompt()
                    .system(systemPrompt.toString())
                    .user(request.mensagem())
                    .call()
                    .content();

            return ResponseEntity.ok(new ChatResponseDto(respostaText));
        } catch (Exception e) {
            return ResponseEntity.ok(new ChatResponseDto("Erro de comunicação com a IA: " + e.getMessage()));
        }
    }
}
