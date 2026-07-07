package gn.odc.gestionrh.auth.dto;

import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.TypeAction;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class AuthReponseDTO {
    private String token;
    private String type = "Bearer";
    private String email;
    private Long employeId;
    private String nomComplet;
    private String prenom;
    private String nom;
    private String photoUrl;
    private Set<String> roles;
    private Map<ModuleApplication, Set<TypeAction>> permissions;
}
