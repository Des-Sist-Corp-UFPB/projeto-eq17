package br.ufpb.dsc.republica.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaForm(
    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 200, message = "A descrição pode ter no máximo 200 caracteres")
    String descricao,

    @NotNull(message = "O valor total é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    BigDecimal valorTotal,

    @NotNull(message = "A data de vencimento é obrigatória")
    LocalDate vencimento,

    @NotNull(message = "O responsável pelo pagamento é obrigatório")
    Long responsavelId
) {}

