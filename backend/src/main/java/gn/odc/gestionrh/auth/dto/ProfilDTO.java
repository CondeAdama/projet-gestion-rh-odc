package gn.odc.gestionrh.auth.dto;

import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.TypeAction;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class ProfilDTO {
    private Long id;
    private String email;
    private boolean actif;
    private boolean confirme;
    private Long employeId;
    private String nomComplet;
    private String nom;
    private String prenom;
    private String telephone;
    private String matricule;
    private String photoUrl;
    private String departementLibelle;
    private String posteLibelle;
    private Set<String> roles;
    private Map<ModuleApplication, Set<TypeAction>> permissions;
}
