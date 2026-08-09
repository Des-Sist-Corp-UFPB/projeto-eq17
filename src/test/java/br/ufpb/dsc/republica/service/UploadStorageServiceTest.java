package br.ufpb.dsc.republica.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UploadStorageServiceTest {

    private UploadStorageService uploadStorageService;
    private List<String> arquivosCriados;

    @BeforeEach
    void setUp() {
        uploadStorageService = new UploadStorageService();
        arquivosCriados = new ArrayList<>();
    }

    @AfterEach
    void tearDown() throws IOException {
        // Limpar todos os arquivos criados nos testes para não poluir o sistema de arquivos
        Path rootPath = Paths.get("uploads", "comprovantes");
        for (String nomeArquivo : arquivosCriados) {
            Path filePath = rootPath.resolve(nomeArquivo);
            Files.deleteIfExists(filePath);
        }
    }

    @Test
    void salvarComprovanteDeveSalvarEretornarNomeSePdfValido() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "comprovante.pdf",
                "application/pdf",
                "conteudo do pdf".getBytes()
        );

        String nomeArquivo = uploadStorageService.salvarComprovante(file);
        
        assertNotNull(nomeArquivo);
        assertTrue(nomeArquivo.endsWith(".pdf"));
        arquivosCriados.add(nomeArquivo);

        Path rootPath = Paths.get("uploads", "comprovantes");
        assertTrue(Files.exists(rootPath.resolve(nomeArquivo)));
    }

    @Test
    void salvarComprovanteDeveSalvarEretornarNomeSePngValido() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "foto.png",
                "image/png",
                "conteudo da imagem".getBytes()
        );

        String nomeArquivo = uploadStorageService.salvarComprovante(file);
        
        assertNotNull(nomeArquivo);
        assertTrue(nomeArquivo.endsWith(".png"));
        arquivosCriados.add(nomeArquivo);

        Path rootPath = Paths.get("uploads", "comprovantes");
        assertTrue(Files.exists(rootPath.resolve(nomeArquivo)));
    }

    @Test
    void salvarComprovanteDeveLancarExcecaoSeArquivoForNulo() {
        assertThrows(IllegalArgumentException.class, () -> uploadStorageService.salvarComprovante(null));
    }

    @Test
    void salvarComprovanteDeveLancarExcecaoSeArquivoForVazio() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "vazio.pdf",
                "application/pdf",
                new byte[0]
        );
        assertThrows(IllegalArgumentException.class, () -> uploadStorageService.salvarComprovante(file));
    }

    @Test
    void salvarComprovanteDeveLancarExcecaoSeContentTypeInvalido() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "texto.txt",
                "text/plain",
                "conteudo".getBytes()
        );
        assertThrows(IllegalArgumentException.class, () -> uploadStorageService.salvarComprovante(file));
    }

    @Test
    void salvarComprovanteDeveLancarExcecaoSeTamanhoExceder5MB() {
        // Cria um byte array de 6MB (6 * 1024 * 1024)
        byte[] grande = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "grande.pdf",
                "application/pdf",
                grande
        );
        assertThrows(IllegalArgumentException.class, () -> uploadStorageService.salvarComprovante(file));
    }

    @Test
    void carregarComprovanteDeveRetornarResourceSeExistir() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "comprovante.pdf",
                "application/pdf",
                "conteudo do pdf".getBytes()
        );

        String nomeArquivo = uploadStorageService.salvarComprovante(file);
        arquivosCriados.add(nomeArquivo);

        Resource resource = uploadStorageService.carregarComprovante(nomeArquivo);

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void carregarComprovanteDeveLancarExcecaoSeNaoExistir() {
        assertThrows(IllegalArgumentException.class, () -> uploadStorageService.carregarComprovante("arquivo_fantasma.pdf"));
    }

    @Test
    void salvarComprovanteDeveSalvarEGerarExtensaoPdfSeNomeNaoTiverExtensaoEPdf() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "comprovante",
                "application/pdf",
                "conteudo do pdf".getBytes()
        );

        String nomeArquivo = uploadStorageService.salvarComprovante(file);
        
        assertNotNull(nomeArquivo);
        assertTrue(nomeArquivo.endsWith(".pdf"));
        arquivosCriados.add(nomeArquivo);

        Path rootPath = Paths.get("uploads", "comprovantes");
        assertTrue(Files.exists(rootPath.resolve(nomeArquivo)));
    }

    @Test
    void salvarComprovanteDeveSalvarEGerarExtensaoPngSeNomeNaoTiverExtensaoEPng() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "comprovante",
                "image/png",
                "conteudo da imagem".getBytes()
        );

        String nomeArquivo = uploadStorageService.salvarComprovante(file);
        
        assertNotNull(nomeArquivo);
        assertTrue(nomeArquivo.endsWith(".png"));
        arquivosCriados.add(nomeArquivo);

        Path rootPath = Paths.get("uploads", "comprovantes");
        assertTrue(Files.exists(rootPath.resolve(nomeArquivo)));
    }

    @Test
    void salvarComprovanteDeveSalvarEGerarExtensaoJpgSeNomeNaoTiverExtensaoEJpeg() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "comprovante",
                "image/jpeg",
                "conteudo da imagem".getBytes()
        );

        String nomeArquivo = uploadStorageService.salvarComprovante(file);
        
        assertNotNull(nomeArquivo);
        assertTrue(nomeArquivo.endsWith(".jpg"));
        arquivosCriados.add(nomeArquivo);

        Path rootPath = Paths.get("uploads", "comprovantes");
        assertTrue(Files.exists(rootPath.resolve(nomeArquivo)));
    }
}
