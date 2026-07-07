package gn.odc.gestionrh.referentiel.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReferentielDTO {
    private Long id;
    private String code;
    private String libelle;
    private String description;
    private String nom;
    private String adresse;
    private String ville;
    private Long departementId;
    private String departementLibelle;
    private String statut;
}
