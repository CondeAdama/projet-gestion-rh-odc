package gn.odc.gestionrh.visite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CarteVisiteRequeteDTO {
    @NotBlank(message = "Le numéro de carte est obligatoire")
    @Size(max = 50)
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "Format invalide (lettres majuscules, chiffres, tirets)")
    private String numeroCarte;
}
