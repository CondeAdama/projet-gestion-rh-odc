package gn.odc.gestionrh.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DemanderReinitialisationDTO {
    @NotBlank @Email
    private String email;
}
