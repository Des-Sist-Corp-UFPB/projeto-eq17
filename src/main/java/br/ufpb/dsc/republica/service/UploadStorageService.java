package br.ufpb.dsc.republica.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.api.trace.Span;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UploadStorageService {

    private final Path rootPath = Paths.get("uploads", "comprovantes");

    public UploadStorageService() {
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads de comprovantes.", e);
        }
    }

    @WithSpan("salvar-comprovante")
    public String salvarComprovante(MultipartFile arquivo) {
        if (arquivo != null) {
            Span.current().setAttribute("comprovante.nome", arquivo.getOriginalFilename());
            Span.current().setAttribute("comprovante.tamanho", arquivo.getSize());
            Span.current().setAttribute("comprovante.tipo", arquivo.getContentType());
        }
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("O arquivo de comprovante não pode ser vazio.");
        }

        // Simulando gargalo lento para exercício de diagnóstico (será removido posteriormente)
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || (!contentType.equals("image/png") && 
                                     !contentType.equals("image/jpeg") && 
                                     !contentType.equals("image/jpg") && 
                                     !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("Formato de arquivo inválido. Apenas PNG, JPEG e PDF são permitidos.");
        }

        // Limita o tamanho do arquivo a 5MB (5 * 1024 * 1024 bytes)
        if (arquivo.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("O arquivo de comprovante excede o tamanho máximo de 5MB.");
        }

        try {
            String nomeOriginal = arquivo.getOriginalFilename();
            String extensao = "";
            if (nomeOriginal != null && nomeOriginal.contains(".")) {
                extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
            }
            
            if (extensao.isEmpty()) {
                if (contentType.equals("application/pdf")) {
                    extensao = ".pdf";
                } else if (contentType.equals("image/png")) {
                    extensao = ".png";
                } else {
                    extensao = ".jpg";
                }
            }

            String nomeArquivoGerado = UUID.randomUUID().toString() + extensao;
            Path targetPath = this.rootPath.resolve(nomeArquivoGerado);
            
            Files.copy(arquivo.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return nomeArquivoGerado;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar o arquivo de comprovante no servidor.", e);
        }
    }

    @WithSpan("carregar-comprovante")
    public Resource carregarComprovante(String nomeArquivo) {
        try {
            Path filePath = this.rootPath.resolve(nomeArquivo).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalArgumentException("Arquivo de comprovante não encontrado ou ilegível.");
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Erro ao recuperar o arquivo de comprovante.", e);
        }
    }
}
