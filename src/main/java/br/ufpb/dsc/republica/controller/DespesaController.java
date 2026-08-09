package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Despesa;
import br.ufpb.dsc.republica.domain.DespesaRateio;
import br.ufpb.dsc.republica.domain.Morador;
import br.ufpb.dsc.republica.domain.Pagamento;
import br.ufpb.dsc.republica.dto.DespesaDto;
import br.ufpb.dsc.republica.dto.DespesaForm;
import br.ufpb.dsc.republica.dto.DespesaRateioDto;
import br.ufpb.dsc.republica.dto.MoradorDto;
import br.ufpb.dsc.republica.repository.DespesaRateioRepository;
import br.ufpb.dsc.republica.repository.PagamentoRepository;
import br.ufpb.dsc.republica.service.AuditoriaService;
import br.ufpb.dsc.republica.service.DespesaService;
import br.ufpb.dsc.republica.service.UploadStorageService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para despesas.
 */
@RestController
@RequestMapping("/api/despesas")
public class DespesaController {

    private final DespesaService despesaService;
    private final DespesaRateioRepository despesaRateioRepository;
    private final PagamentoRepository pagamentoRepository;
    private final UploadStorageService uploadStorageService;
    private final AuditoriaService auditoriaService;

    public DespesaController(DespesaService despesaService,
                             DespesaRateioRepository despesaRateioRepository, 
                             PagamentoRepository pagamentoRepository,
                             UploadStorageService uploadStorageService,
                             AuditoriaService auditoriaService) {
        this.despesaService = despesaService;
        this.despesaRateioRepository = despesaRateioRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.uploadStorageService = uploadStorageService;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Cadastra uma nova despesa em uma casa.
     */
    @PostMapping("/casa/{casaId}")
    public ResponseEntity<DespesaDto> cadastrarDespesa(@PathVariable("casaId") Long casaId,
                                                       @Valid @RequestBody DespesaForm form) {
        Despesa despesa = despesaService.cadastrarDespesa(casaId, form);
        return ResponseEntity.ok(toDespesaDto(despesa));
    }

    /**
     * Retorna os rateios detalhados de uma despesa.
     */
    @GetMapping("/{id}/rateios")
    public ResponseEntity<List<DespesaRateioDto>> verRateios(@PathVariable("id") Long id) {
        Despesa despesa = despesaService.buscarPorId(id);
        List<DespesaRateioDto> rateiosDto = despesa.getRateios().stream()
                .map(this::toDespesaRateioDto)
                .toList();
        return ResponseEntity.ok(rateiosDto);
    }

    /**
     * Registra o comprovante de pagamento de um rateio feito por um morador via upload de arquivo (Imagem ou PDF).
     */
    @PostMapping(value = "/rateio/{rateioId}/pagar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DespesaDto> registrarPagamento(@PathVariable("rateioId") Long rateioId,
                                                         @RequestParam("comprovante") MultipartFile comprovante) {
        if (comprovante == null || comprovante.isEmpty()) {
            throw new IllegalArgumentException("O arquivo de comprovante é obrigatório.");
        }

        despesaService.informarPagamentoComArquivo(rateioId, comprovante);

        Despesa despesa = despesaService.buscarPorId(idDaDespesaDoRateio(rateioId));
        return ResponseEntity.ok(toDespesaDto(despesa));
    }

    /**
     * Retorna os bytes do comprovante de pagamento de forma protegida apenas para usuários autorizados.
     */
    @GetMapping("/pagamento/{pagamentoId}/comprovante")
    public ResponseEntity<Resource> obterComprovante(@PathVariable("pagamentoId") Long pagamentoId,
                                                     Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        // Verifica as regras de acesso LGPD
        if (!despesaService.podeVisualizarComprovante(pagamentoId, principal.getName())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento não encontrado."));

        if (pagamento.getComprovante() == null || pagamento.getComprovante().trim().isEmpty()) {
            throw new IllegalArgumentException("Nenhum comprovante associado a este pagamento.");
        }

        Resource arquivo = uploadStorageService.carregarComprovante(pagamento.getComprovante());

        // Detecta Content-Type dinamicamente
        String contentType = "application/octet-stream";
        String nomeArquivo = arquivo.getFilename();
        if (nomeArquivo != null) {
            if (nomeArquivo.toLowerCase().endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (nomeArquivo.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (nomeArquivo.toLowerCase().endsWith(".jpg") || nomeArquivo.toLowerCase().endsWith(".jpeg")) {
                contentType = "image/jpeg";
            }
        }

        // Auditoria: Visualização de comprovante
        auditoriaService.registrarAcaoUsuarioLogado("VISUALIZAR_COMPROVANTE", 
                String.format("Visualizou o arquivo de comprovante do pagamento id %d.", pagamentoId), 
                "Pagamento", pagamentoId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nomeArquivo + "\"")
                .body(arquivo);
    }

    /**
     * Confirma um pagamento.
     */
    @PostMapping("/pagamento/{pagamentoId}/confirmar")
    public ResponseEntity<DespesaDto> confirmarPagamento(@PathVariable("pagamentoId") Long pagamentoId) {
        despesaService.confirmarPagamento(pagamentoId);
        Despesa despesa = despesaService.buscarPorId(idDaDespesaDoPagamento(pagamentoId));
        return ResponseEntity.ok(toDespesaDto(despesa));
    }

    /**
     * Rejeita um pagamento.
     */
    @PostMapping("/pagamento/{pagamentoId}/rejeitar")
    public ResponseEntity<DespesaDto> rejeitarPagamento(@PathVariable("pagamentoId") Long pagamentoId) {
        despesaService.rejeitarPagamento(pagamentoId);
        Despesa despesa = despesaService.buscarPorId(idDaDespesaDoPagamento(pagamentoId));
        return ResponseEntity.ok(toDespesaDto(despesa));
    }

    /**
     * Exclui logicamente uma despesa.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> excluirDespesa(@PathVariable("id") Long id) {
        despesaService.excluirDespesa(id);
        return ResponseEntity.ok(Map.of("mensagem", "Despesa excluída com sucesso."));
    }

    private DespesaDto toDespesaDto(Despesa d) {
        return new DespesaDto(
                d.getId(),
                d.getDescricao(),
                d.getValorTotal(),
                d.getVencimento(),
                d.getStatus(),
                toMoradorDto(d.getResponsavel()),
                d.getRateios().stream()
                        .map(this::toDespesaRateioDto)
                        .toList(),
                d.getTipo(),
                d.getChavePix()
        );
    }

    private DespesaRateioDto toDespesaRateioDto(DespesaRateio r) {
        return new DespesaRateioDto(
                r.getId(),
                toMoradorDto(r.getMorador()),
                r.getValorDevido(),
                r.getUltimoPagamento() != null ? r.getUltimoPagamento().getStatus() : null,
                r.getUltimoPagamento() != null ? r.getUltimoPagamento().getId() : null,
                r.getUltimoPagamento() != null ? r.getUltimoPagamento().getComprovante() : null,
                r.getUltimoPagamento() != null ? r.getUltimoPagamento().getDataPagamento() : null
        );
    }

    private MoradorDto toMoradorDto(Morador m) {
        if (m == null) {
            return null;
        }
        return new MoradorDto(
                m.getId(),
                m.getUsuario().getNome(),
                m.getUsuario().getEmail(),
                m.getPapel()
        );
    }

    private Long idDaDespesaDoRateio(Long rateioId) {
        return despesaRateioRepository.findById(rateioId)
                .map(r -> r.getDespesa().getId())
                .orElseThrow(() -> new IllegalArgumentException("Rateio não encontrado."));
    }

    private Long idDaDespesaDoPagamento(Long pagamentoId) {
        return pagamentoRepository.findById(pagamentoId)
                .map(p -> p.getRateio().getDespesa().getId())
                .orElseThrow(() -> new IllegalArgumentException("Pagamento não encontrado."));
    }
}
