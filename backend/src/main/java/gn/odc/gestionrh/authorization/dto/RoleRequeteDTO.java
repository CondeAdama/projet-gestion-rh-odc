package gn.odc.gestionrh.authorization.dto;

import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.TypeAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class RoleRequeteDTO {
    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 50)
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Code invalide (majuscules, chiffres, underscore)")
    private String code;

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 100)
    private String libelle;

    @Size(max = 255)
    private String description;

    @NotEmpty(message = "Au moins une permission est requise")
    private Map<ModuleApplication, Set<TypeAction>> permissions;
}
