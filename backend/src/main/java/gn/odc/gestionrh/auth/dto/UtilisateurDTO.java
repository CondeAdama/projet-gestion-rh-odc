package gn.odc.gestionrh.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UtilisateurDTO {
    private Long id;
    private String email;
    private boolean actif;
    private boolean confirme;
    private Long employeId;
    private String nomComplet;
    private String matricule;
    private String telephone;
    private String departementLibelle;
    private String posteLibelle;
    private Set<String> roles;
    private String statut;
}
