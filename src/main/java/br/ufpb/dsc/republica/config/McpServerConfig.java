package br.ufpb.dsc.republica.config;

import br.ufpb.dsc.republica.service.RepublicaTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider republicaToolsCallbackProvider(RepublicaTools republicaTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(republicaTools)
                .build();
    }
}
