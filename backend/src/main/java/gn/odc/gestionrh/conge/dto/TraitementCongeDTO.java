package gn.odc.gestionrh.conge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TraitementCongeDTO {
    @NotBlank(message = "Le statut est obligatoire")
    @Pattern(regexp = "APPROUVE|REFUSE", message = "Statut invalide (APPROUVE ou REFUSE)")
    private String statut;

    @Size(max = 500, message = "Le commentaire ne doit pas dépasser 500 caractères")
    private String commentaireRh;
}
