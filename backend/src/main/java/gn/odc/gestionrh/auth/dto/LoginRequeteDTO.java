package gn.odc.gestionrh.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequeteDTO {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String motDePasse;
}
