package gn.odc.gestionrh.authorization.service;

import gn.odc.gestionrh.auth.security.UtilisateurPrincipal;
import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.TypeAction;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccesEmployeHelper {

    private final EvaluateurAutorisation evaluateurAutorisation;

    public Long employeId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            return null;
        }
        return principal.getEmployeId();
    }

    public Long requireEmployeId(Authentication auth) {
        Long id = employeId(auth);
        if (id == null) {
            throw new RegleMetierException("Aucun profil employé associé à ce compte");
        }
        return id;
    }

    public boolean peutVoirAutrui(Authentication auth, ModuleApplication module) {
        return evaluateurAutorisation.aPermission(auth, module, TypeAction.AFFICHER_AUTRUI);
    }

    public void verifierPropreEmployeOuAutrui(Authentication auth, ModuleApplication module, Long cibleEmployeId) {
        if (peutVoirAutrui(auth, module)) {
            return;
        }
        Long own = requireEmployeId(auth);
        if (cibleEmployeId == null || !cibleEmployeId.equals(own)) {
            throw new org.springframework.security.access.AccessDeniedException("Accès refusé aux données d'un autre employé");
        }
    }
}
