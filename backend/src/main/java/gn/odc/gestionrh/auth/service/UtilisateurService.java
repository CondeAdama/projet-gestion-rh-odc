package gn.odc.gestionrh.auth.service;

import gn.odc.gestionrh.auth.dto.UtilisateurDTO;
import gn.odc.gestionrh.auth.dto.UtilisateurModifierDTO;
import gn.odc.gestionrh.auth.entity.Utilisateur;
import gn.odc.gestionrh.auth.repository.UtilisateurRepository;
import gn.odc.gestionrh.authorization.entity.Role;
import gn.odc.gestionrh.authorization.repository.RoleRepository;
import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.common.service.ServiceSuppression;
import gn.odc.gestionrh.employe.entity.Employe;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final CompteProvisionService compteProvisionService;

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listerActifs() {
        return utilisateurRepository.findByStatut(StatutEntite.ACTIF).stream()
                .map(this::versDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listerCorbeille() {
        return utilisateurRepository.findByStatut(StatutEntite.SUPPRIME).stream()
                .map(this::versDTO).toList();
    }

    @Transactional
    public UtilisateurDTO modifier(Long id, UtilisateurModifierDTO dto) {
        Utilisateur u = ServiceSuppression.trouverActif(utilisateurRepository, id, "Utilisateur");

        if (dto.getRoleCodes() != null && !dto.getRoleCodes().isEmpty()) {
            if (dto.getRoleCodes().contains("ADMINISTRATEUR") && !connecteEstAdministrateur()) {
                throw new RegleMetierException("Seul un administrateur peut attribuer le rôle Administrateur Système");
            }
            Set<Role> roles = new HashSet<>();
            for (String code : dto.getRoleCodes()) {
                Role role = roleRepository.findByCode(code)
                        .orElseThrow(() -> new RegleMetierException("Rôle introuvable : " + code));
                roles.add(role);
            }
            u.setRoles(roles);
        }

        if (dto.getActif() != null) {
            u.setActif(dto.getActif());
        }

        return versDTO(utilisateurRepository.save(u));
    }

    @Transactional
    public void supprimer(Long id) {
        Utilisateur u = ServiceSuppression.trouverActif(utilisateurRepository, id, "Utilisateur");
        if (u.getRoles().stream().anyMatch(r -> "ADMINISTRATEUR".equals(r.getCode()))) {
            throw new RegleMetierException("Impossible de supprimer le compte administrateur");
        }
        u.setActif(false);
        ServiceSuppression.supprimerLogique(u);
        utilisateurRepository.save(u);
    }

    @Transactional
    public UtilisateurDTO restaurer(Long id) {
        Utilisateur u = ServiceSuppression.trouverSupprime(utilisateurRepository, id, "Utilisateur");
        ServiceSuppression.restaurer(u);
        u.setActif(true);
        return versDTO(utilisateurRepository.save(u));
    }

    @Transactional
    public java.util.Map<String, String> renvoyerActivation(Long id) {
        Utilisateur u = ServiceSuppression.trouverActif(utilisateurRepository, id, "Utilisateur");
        return compteProvisionService.renvoyerActivation(u.getEmail());
    }

    private boolean connecteEstAdministrateur() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRATEUR".equals(a.getAuthority()));
    }

    private UtilisateurDTO versDTO(Utilisateur u) {
        Employe e = u.getEmploye();
        Set<String> roles = u.getRoles().stream().map(Role::getCode).collect(Collectors.toSet());
        return UtilisateurDTO.builder()
                .id(u.getId())
                .email(u.getEmail())
                .actif(u.isActif())
                .confirme(u.isConfirme())
                .employeId(e != null ? e.getId() : null)
                .nomComplet(e != null ? e.getPrenom() + " " + e.getNom() : null)
                .matricule(e != null ? e.getMatricule() : null)
                .telephone(e != null ? e.getTelephone() : null)
                .departementLibelle(e != null && e.getDepartement() != null ? e.getDepartement().getLibelle() : null)
                .posteLibelle(e != null && e.getPoste() != null ? e.getPoste().getLibelle() : null)
                .roles(roles)
                .statut(u.getStatut().name())
                .build();
    }
}
