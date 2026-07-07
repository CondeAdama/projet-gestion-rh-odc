package gn.odc.gestionrh.configuration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestEmailDTO {
    @NotBlank
    @Email
    private String email;
}
