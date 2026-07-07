package gn.odc.gestionrh.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActiverCompteRequeteDTO {
    @NotBlank @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "Le code doit contenir 6 chiffres")
    private String code;

    @NotBlank
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Le mot de passe doit contenir une lettre et un chiffre")
    private String motDePasse;

    private String token;
}
