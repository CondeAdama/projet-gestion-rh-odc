package gn.odc.gestionrh.configuration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TestSmsDTO {
    @NotBlank
    @Pattern(regexp = "^\\+?[0-9\\s\\-]{8,20}$", message = "Format de téléphone invalide")
    private String telephone;
}
