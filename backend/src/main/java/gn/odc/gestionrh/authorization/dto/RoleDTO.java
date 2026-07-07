package gn.odc.gestionrh.authorization.dto;

import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.TypeAction;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
@Builder
public class RoleDTO {
    private Long id;
    private String code;
    private String libelle;
    private String description;
    private boolean systeme;
    private boolean parDefaut;
    private String statut;
    private Set<PermissionDTO> permissions;
    private Map<ModuleApplication, Set<TypeAction>> matrice;
}
