package gn.odc.gestionrh.visite.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VisiteurDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String contact;
    private String entreprise;
    private String statut;
}
