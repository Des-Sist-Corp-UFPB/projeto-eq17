package br.ufpb.dsc.republica.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários para o PingController focados em cenários simulados.
 * Utiliza @WebMvcTest para evitar subir o banco de dados real.
 */
@WebMvcTest(PingController.class)
public class PingControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testPingEndpointWhenDatabaseIsDown() throws Exception {
        // Simula uma falha ao acessar o banco de dados
        Mockito.when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
               .thenThrow(new RuntimeException("Database connection timeout"));

        mockMvc.perform(get("/ping")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.database").value("down"))
                .andExpect(jsonPath("$.service").value("eq17"))
                .andExpect(jsonPath("$.error").value("Database connection timeout"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void testPingEndpointWhenDatabaseReturnsUnexpectedValue() throws Exception {
        // Simula o banco retornando algo diferente de 1 (por exemplo, null ou 0)
        Mockito.when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
               .thenReturn(null);

        mockMvc.perform(get("/ping")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.database").value("down"))
                .andExpect(jsonPath("$.service").value("eq17"))
                .andExpect(jsonPath("$.error").value("Unexpected response from database"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
