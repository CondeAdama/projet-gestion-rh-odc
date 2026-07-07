package gn.odc.gestionrh.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UtilisateurModifierDTO {
    @NotEmpty(message = "Au moins un rôle est requis")
    private Set<String> roleCodes;
    private Boolean actif;
}
