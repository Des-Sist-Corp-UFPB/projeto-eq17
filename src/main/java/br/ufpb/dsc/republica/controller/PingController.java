package br.ufpb.dsc.republica.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * Controller para expor o endpoint de health check /ping requerido para status do projeto.
 * Este endpoint retorna informações básicas sobre a saúde do microsserviço "eq17".
 */
@RestController
public class PingController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
            "status", "ok",
            "service", "eq17",
            "timestamp", java.time.Instant.now().toString()
        );
    }
}
