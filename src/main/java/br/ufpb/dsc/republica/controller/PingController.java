package br.ufpb.dsc.republica.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import br.ufpb.dsc.republica.service.UploadStorageService;
import br.ufpb.dsc.republica.service.EmailService;
import br.ufpb.dsc.republica.domain.Usuario;
import org.springframework.web.multipart.MultipartFile;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller para expor o endpoint de health check /ping requerido para status do projeto.
 * Este endpoint retorna informações básicas sobre a saúde do microsserviço "eq17" e do banco de dados.
 */
@RestController
public class PingController {

    private final JdbcTemplate jdbcTemplate;
    private final UploadStorageService uploadStorageService;
    private final EmailService emailService;

    public PingController(JdbcTemplate jdbcTemplate, UploadStorageService uploadStorageService, EmailService emailService) {
        this.jdbcTemplate = jdbcTemplate;
        this.uploadStorageService = uploadStorageService;
        this.emailService = emailService;
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

    @GetMapping("/ping/test-otel")
    public ResponseEntity<Map<String, Object>> testOtel() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "running_otel_test");

        // 1. Simula arquivo comprovante para UploadStorageService
        MultipartFile mockFile = new MultipartFile() {
            @Override public String getName() { return "comprovante"; }
            @Override public String getOriginalFilename() { return "comprovante_aluguel.png"; }
            @Override public String getContentType() { return "image/png"; }
            @Override public boolean isEmpty() { return false; }
            @Override public long getSize() { return 1024L; }
            @Override public byte[] getBytes() { return new byte[1024]; }
            @Override public java.io.InputStream getInputStream() { return new java.io.ByteArrayInputStream(new byte[1024]); }
            @Override public void transferTo(java.io.File dest) {}
        };
        
        String nomeComprovante = uploadStorageService.salvarComprovante(mockFile);
        response.put("uploaded_file", nomeComprovante);

        // 2. Simula envio de e-mail de confirmação
        Usuario mockUser = new Usuario();
        mockUser.setNome("Maria OTel");
        mockUser.setEmail("maria.otel@eq17.com");
        emailService.enviarEmailConfirmacao(mockUser, "token-teste-otel-123");
        response.put("email_simulated", "ok");

        // 3. Executa query SQL real no banco para aparecer no trace
        try {
            jdbcTemplate.execute("SELECT count(*) FROM usuario");
            response.put("db_query", "ok");
        } catch (Exception e) {
            response.put("db_query", "failed: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
