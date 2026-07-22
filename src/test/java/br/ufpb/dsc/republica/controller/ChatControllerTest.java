package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.config.CustomOAuth2SuccessHandler;
import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.ChatRequestDto;
import br.ufpb.dsc.republica.repository.MoradorRepository;
import br.ufpb.dsc.republica.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatController.class, properties = "spring.main.allow-bean-definition-overriding=true", excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@Import(ChatControllerTest.TestConfig.class)
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private MoradorRepository moradorRepository;

    @MockitoBean
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    private Usuario usuario;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ChatClient.Builder chatClientBuilder() {
            ChatClient.Builder builder = mock(ChatClient.Builder.class);
            ChatClient mockChatClient = mock(ChatClient.class);
            ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
            ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);

            when(mockChatClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.system(anyString())).thenReturn(requestSpec);
            when(requestSpec.user(anyString())).thenReturn(requestSpec);
            when(requestSpec.call()).thenReturn(responseSpec);
            when(responseSpec.content()).thenReturn("Olá! Sou o assistente da república e posso te ajudar.");

            when(builder.defaultTools(any())).thenReturn(builder);
            when(builder.defaultTools(any(Object[].class))).thenReturn(builder);
            when(builder.defaultTools(any(ToolCallback[].class))).thenReturn(builder);
            when(builder.build()).thenReturn(mockChatClient);

            return builder;
        }

        @Bean
        public ToolCallbackProvider toolCallbackProvider() {
            ToolCallbackProvider provider = mock(ToolCallbackProvider.class);
            when(provider.getToolCallbacks()).thenReturn(new ToolCallback[0]);
            return provider;
        }
    }

    @BeforeEach
    void setUp() {
        usuario = new Usuario("Ramon Teste", "ramon.chat@test.com", "senha123");
        usuario.setEmailConfirmado(true);
    }

    @Test
    @WithMockUser(username = "ramon.chat@test.com")
    void conversarComSucesso() throws Exception {
        when(usuarioRepository.findByEmail("ramon.chat@test.com")).thenReturn(Optional.of(usuario));
        when(moradorRepository.findByUsuarioId(any())).thenReturn(new ArrayList<>());

        ChatRequestDto request = new ChatRequestDto("Olá assistente!");

        mockMvc.perform(post("/api/chat")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resposta").value("Olá! Sou o assistente da república e posso te ajudar."));
    }

    @Test
    void conversarSemAutenticacaoRetornaUnauthorized() throws Exception {
        ChatRequestDto request = new ChatRequestDto("Olá!");

        mockMvc.perform(post("/api/chat")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is3xxRedirection());
    }
}
