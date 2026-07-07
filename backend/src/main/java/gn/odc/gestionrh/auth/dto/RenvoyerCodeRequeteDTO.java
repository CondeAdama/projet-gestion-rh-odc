package gn.odc.gestionrh.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RenvoyerCodeRequeteDTO {
    @NotBlank(message = "L'e-mail est obligatoire")
    @Email(message = "Format d'e-mail invalide")
    private String email;
}
