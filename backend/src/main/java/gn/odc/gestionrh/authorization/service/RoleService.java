package gn.odc.gestionrh.authorization.service;

import gn.odc.gestionrh.authorization.dto.PermissionDTO;
import gn.odc.gestionrh.authorization.dto.RoleDTO;
import gn.odc.gestionrh.authorization.dto.RoleRequeteDTO;
import gn.odc.gestionrh.authorization.entity.Permission;
import gn.odc.gestionrh.authorization.entity.Role;
import gn.odc.gestionrh.authorization.repository.PermissionRepository;
import gn.odc.gestionrh.authorization.repository.RoleRepository;
import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.common.enums.TypeAction;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.common.exception.RessourceNonTrouveeException;
import gn.odc.gestionrh.common.service.ServiceSuppression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<RoleDTO> listerActifs() {
        return roleRepository.findByStatut(StatutEntite.ACTIF).stream()
                .map(this::versDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> listerSupprimes() {
        return roleRepository.findByStatut(StatutEntite.SUPPRIME).stream()
                .map(this::versDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleDTO trouverParId(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Rôle introuvable"));
        return versDTO(role);
    }

    @Transactional(readOnly = true)
    public RoleDTO obtenirParDefaut() {
        Role role = roleRepository.findByParDefautTrueAndStatut(StatutEntite.ACTIF)
                .orElseGet(() -> roleRepository.findByCode("EMPLOYE")
                        .orElseThrow(() -> new RessourceNonTrouveeException("Aucun rôle par défaut configuré")));
        return versDTO(role);
    }

    @Transactional
    public RoleDTO definirParDefaut(Long id) {
        Role cible = roleRepository.findById(id)
                .filter(r -> r.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Rôle introuvable"));
        if ("ADMINISTRATEUR".equals(cible.getCode())) {
            throw new RegleMetierException("Le rôle Administrateur ne peut pas être défini par défaut");
        }
        roleRepository.findByParDefautTrueAndStatut(StatutEntite.ACTIF).ifPresent(r -> {
            if (!r.getId().equals(cible.getId())) {
                r.setParDefaut(false);
                roleRepository.save(r);
            }
        });
        cible.setParDefaut(true);
        return versDTO(roleRepository.save(cible));
    }

    @Transactional(readOnly = true)
    public String codeRoleParDefaut() {
        return roleRepository.findByParDefautTrueAndStatut(StatutEntite.ACTIF)
                .map(Role::getCode)
                .orElse("EMPLOYE");
    }

    @Transactional
    public RoleDTO creer(RoleRequeteDTO requete) {
        if (roleRepository.existsByCode(requete.getCode())) {
            throw new RegleMetierException("Un rôle avec ce code existe déjà");
        }
        Role role = Role.builder()
                .code(requete.getCode().toUpperCase())
                .libelle(requete.getLibelle())
                .description(requete.getDescription())
                .systeme(false)
                .permissions(resoudrePermissions(requete.getPermissions()))
                .build();
        return versDTO(roleRepository.save(role));
    }

    @Transactional
    public RoleDTO modifier(Long id, RoleRequeteDTO requete) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Rôle introuvable"));
        if (role.isSysteme()) {
            throw new RegleMetierException("Les rôles système ne peuvent pas être modifiés");
        }
        role.setLibelle(requete.getLibelle());
        role.setDescription(requete.getDescription());
        role.setPermissions(resoudrePermissions(requete.getPermissions()));
        return versDTO(roleRepository.save(role));
    }

    @Transactional
    public void supprimerLogique(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Rôle introuvable"));
        if (role.isSysteme()) {
            throw new RegleMetierException("Les rôles système ne peuvent pas être supprimés");
        }
        ServiceSuppression.supprimerLogique(role);
        roleRepository.save(role);
    }

    @Transactional
    public RoleDTO restaurer(Long id) {
        Role role = ServiceSuppression.trouverSupprime(roleRepository, id, "Rôle");
        ServiceSuppression.restaurer(role);
        return versDTO(roleRepository.save(role));
    }

    @Transactional
    public void supprimerDefinitif(Long id) {
        Role role = ServiceSuppression.trouverSupprime(roleRepository, id, "Rôle");
        if (role.isSysteme()) {
            throw new RegleMetierException("Les rôles système ne peuvent pas être supprimés définitivement");
        }
        roleRepository.delete(role);
    }

    @Transactional(readOnly = true)
    public List<PermissionDTO> listerToutesPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::versPermissionDTO)
                .toList();
    }

    private Set<Permission> resoudrePermissions(Map<ModuleApplication, Set<TypeAction>> matrice) {
        if (matrice == null || matrice.isEmpty()) {
            return new HashSet<>();
        }
        Set<Permission> permissions = new HashSet<>();
        for (Map.Entry<ModuleApplication, Set<TypeAction>> entry : matrice.entrySet()) {
            for (TypeAction action : entry.getValue()) {
                Permission permission = permissionRepository
                        .findByModuleAndAction(entry.getKey(), action)
                        .orElseThrow(() -> new RegleMetierException(
                                "Permission introuvable : " + entry.getKey() + ":" + action));
                permissions.add(permission);
            }
        }
        return permissions;
    }

    private RoleDTO versDTO(Role role) {
        Set<PermissionDTO> perms = role.getPermissions().stream()
                .map(this::versPermissionDTO)
                .collect(Collectors.toSet());

        Map<ModuleApplication, Set<TypeAction>> matrice = new EnumMap<>(ModuleApplication.class);
        for (PermissionDTO p : perms) {
            matrice.computeIfAbsent(p.getModule(), k -> new HashSet<>()).add(p.getAction());
        }

        return RoleDTO.builder()
                .id(role.getId())
                .code(role.getCode())
                .libelle(role.getLibelle())
                .description(role.getDescription())
                .systeme(role.isSysteme())
                .parDefaut(role.isParDefaut())
                .statut(role.getStatut().name())
                .permissions(perms)
                .matrice(matrice)
                .build();
    }

    private PermissionDTO versPermissionDTO(Permission p) {
        return PermissionDTO.builder()
                .module(p.getModule())
                .action(p.getAction())
                .cle(p.getCle())
                .build();
    }
}
