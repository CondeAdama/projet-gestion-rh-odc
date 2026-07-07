package gn.odc.gestionrh.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MotDePasseModifierDTO {
    @NotBlank
    private String motDePasseActuel;

    @NotBlank
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Le mot de passe doit contenir une lettre et un chiffre")
    private String nouveauMotDePasse;
}
