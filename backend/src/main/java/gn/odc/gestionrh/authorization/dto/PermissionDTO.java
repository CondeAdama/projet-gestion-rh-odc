package gn.odc.gestionrh.authorization.dto;

import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.TypeAction;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionDTO {
    private ModuleApplication module;
    private TypeAction action;
    private String cle;
}
