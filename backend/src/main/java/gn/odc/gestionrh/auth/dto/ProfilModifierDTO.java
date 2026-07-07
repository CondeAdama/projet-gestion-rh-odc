package gn.odc.gestionrh.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfilModifierDTO {
    @Size(max = 100)
    private String prenom;

    @Size(max = 100)
    private String nom;

    @Pattern(regexp = "^\\+?[0-9\\s\\-]{8,20}$", message = "Format de téléphone invalide")
    private String telephone;
}
