package gn.odc.gestionrh.authorization.service;

import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.TypeAction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("autorisation")
public class EvaluateurAutorisation {

    public boolean aPermission(Authentication auth, ModuleApplication module, TypeAction action) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        String cle = module.name() + ":" + action.name();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(cle) || a.equals("ROLE_ADMINISTRATEUR"));
    }
}
