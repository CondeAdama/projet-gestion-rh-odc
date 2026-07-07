package gn.odc.gestionrh.paie.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class GenererPaieRequeteDTO {
    @NotNull(message = "L'employé est obligatoire")
    private Long employeId;

    @Min(value = 1, message = "Le mois doit être entre 1 et 12")
    @Max(value = 12, message = "Le mois doit être entre 1 et 12")
    private int mois;

    @Min(value = 2000, message = "Année invalide")
    @Max(value = 2100, message = "Année invalide")
    private int annee;
}
