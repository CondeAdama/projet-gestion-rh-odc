package gn.odc.gestionrh.referentiel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReferentielRequeteDTO {
    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 20)
    private String code;

    @Size(max = 150)
    private String libelle;

    @Size(max = 255)
    private String description;

    @Size(max = 150)
    private String nom;

    @Size(max = 255)
    private String adresse;

    @Size(max = 100)
    private String ville;

    private Long departementId;
}
