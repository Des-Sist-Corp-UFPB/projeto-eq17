package br.ufpb.dsc.republica.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller para expor o endpoint de health check /ping requerido para status do projeto.
 * Este endpoint retorna informações básicas sobre a saúde do microsserviço "eq17" e do banco de dados.
 */
@RestController
public class PingController {

    private final JdbcTemplate jdbcTemplate;

    public PingController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "eq17");
        response.put("timestamp", java.time.Instant.now().toString());

        try {
            // Executa um teste simples no banco de dados para verificar a saúde da conexão
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                response.put("status", "ok");
                response.put("database", "ok");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("database", "down");
                response.put("error", "Unexpected response from database");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("database", "down");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
