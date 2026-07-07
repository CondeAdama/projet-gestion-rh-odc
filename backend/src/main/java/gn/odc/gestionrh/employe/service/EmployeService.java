package gn.odc.gestionrh.employe.service;

import gn.odc.gestionrh.auth.repository.UtilisateurRepository;
import gn.odc.gestionrh.auth.service.CompteProvisionService;
import gn.odc.gestionrh.authorization.entity.Role;
import gn.odc.gestionrh.authorization.repository.RoleRepository;
import gn.odc.gestionrh.authorization.service.RoleService;
import gn.odc.gestionrh.common.enums.StatutEmploi;
import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.common.exception.RessourceNonTrouveeException;
import gn.odc.gestionrh.common.service.ServiceSuppression;
import gn.odc.gestionrh.contrat.repository.ContratRepository;
import gn.odc.gestionrh.employe.dto.EmployeDTO;
import gn.odc.gestionrh.employe.dto.EmployeRequeteDTO;
import gn.odc.gestionrh.employe.entity.Employe;
import gn.odc.gestionrh.employe.repository.EmployeRepository;
import gn.odc.gestionrh.notification.service.NotificationService;
import gn.odc.gestionrh.referentiel.entity.Departement;
import gn.odc.gestionrh.referentiel.entity.Poste;
import gn.odc.gestionrh.referentiel.repository.DepartementRepository;
import gn.odc.gestionrh.referentiel.repository.PosteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final PosteRepository posteRepository;
    private final ContratRepository contratRepository;
    private final NotificationService notificationService;
    private final CompteProvisionService compteProvisionService;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final RoleService roleService;

    @Transactional(readOnly = true)
    public List<EmployeDTO> listerActifs() {
        return employeRepository.findByStatut(StatutEntite.ACTIF).stream()
                .map(this::versDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeDTO> listerSupprimes() {
        return employeRepository.findByStatut(StatutEntite.SUPPRIME).stream()
                .map(this::versDTO).toList();
    }

    @Transactional(readOnly = true)
    public EmployeDTO trouverParId(Long id) {
        Employe e = ServiceSuppression.trouverActif(employeRepository, id, "Employé");
        return versDTO(e);
    }

    @Transactional(readOnly = true)
    public EmployeDTO trouverParMatricule(String matricule) {
        Employe e = employeRepository.findByMatricule(matricule)
                .filter(emp -> emp.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Employé introuvable : " + matricule));
        return versDTO(e);
    }

    @Transactional
    public EmployeDTO creer(EmployeRequeteDTO req) {
        String roleCode = (req.getRoleCode() == null || req.getRoleCode().isBlank())
                ? roleService.codeRoleParDefaut()
                : req.getRoleCode().trim().toUpperCase();
        if ("ADMINISTRATEUR".equals(roleCode)) {
            throw new RegleMetierException("Le rôle Administrateur ne peut pas être attribué via la création d'employé");
        }
        validerUnicite(req, null);
        Employe employe = mapperRequete(req, new Employe());
        employe = employeRepository.save(employe);
        compteProvisionService.provisionnerCompte(employe, roleCode);
        return versDTO(employe);
    }

    @Transactional
    public EmployeDTO modifier(Long id, EmployeRequeteDTO req) {
        Employe employe = ServiceSuppression.trouverActif(employeRepository, id, "Employé");
        validerUnicite(req, id);
        mapperRequete(req, employe);
        if (req.getRoleCode() != null && !req.getRoleCode().isBlank()) {
            mettreAJourRoleUtilisateur(employe, req.getRoleCode().trim().toUpperCase());
        }
        return versDTO(employeRepository.save(employe));
    }

    private void mettreAJourRoleUtilisateur(Employe employe, String roleCode) {
        if ("ADMINISTRATEUR".equals(roleCode)) {
            throw new RegleMetierException("Le rôle Administrateur ne peut pas être attribué depuis la fiche employé");
        }
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new RegleMetierException("Rôle introuvable : " + roleCode));
        utilisateurRepository.findByEmail(employe.getEmail()).ifPresent(u -> {
            u.setRoles(Set.of(role));
            utilisateurRepository.save(u);
        });
    }

    @Transactional
    public void supprimerLogique(Long id) {
        Employe employe = ServiceSuppression.trouverActif(employeRepository, id, "Employé");
        ServiceSuppression.supprimerLogique(employe);
        employeRepository.save(employe);
    }

    @Transactional
    public EmployeDTO restaurer(Long id) {
        Employe employe = ServiceSuppression.trouverSupprime(employeRepository, id, "Employé");
        ServiceSuppression.restaurer(employe);
        return versDTO(employeRepository.save(employe));
    }

    @Transactional
    public void supprimerDefinitif(Long id) {
        Employe employe = ServiceSuppression.trouverSupprime(employeRepository, id, "Employé");
        employeRepository.delete(employe);
    }

    @Transactional
    public EmployeDTO licencier(Long id) {
        Employe employe = ServiceSuppression.trouverActif(employeRepository, id, "Employé");
        employe.setStatutEmploi(StatutEmploi.LICENCIE);
        Employe sauvegarde = employeRepository.save(employe);

        notificationService.envoyerLicenciement(employe);
        compteProvisionService.desactiverCompte(employe);

        return versDTO(sauvegarde);
    }

    @Transactional
    public EmployeDTO suspendre(Long id) {
        Employe employe = ServiceSuppression.trouverActif(employeRepository, id, "Employé");
        employe.setStatutEmploi(StatutEmploi.SUSPENDU);
        Employe sauvegarde = employeRepository.save(employe);

        notificationService.envoyerSuspension(employe);
        compteProvisionService.desactiverCompte(employe);

        return versDTO(sauvegarde);
    }

    @Transactional
    public Map<String, String> renvoyerActivation(Long id) {
        Employe employe = ServiceSuppression.trouverActif(employeRepository, id, "Employé");
        return compteProvisionService.renvoyerActivation(employe.getEmail());
    }

    private void validerUnicite(EmployeRequeteDTO req, Long idExclu) {
        boolean matriculeExiste = idExclu == null
                ? employeRepository.existsByMatricule(req.getMatricule())
                : employeRepository.existsByMatriculeAndIdNot(req.getMatricule(), idExclu);
        if (matriculeExiste) {
            throw new RegleMetierException("Le matricule " + req.getMatricule() + " est déjà utilisé");
        }
        boolean emailExiste = idExclu == null
                ? employeRepository.existsByEmail(req.getEmail())
                : employeRepository.existsByEmailAndIdNot(req.getEmail().toLowerCase(), idExclu);
        if (emailExiste) {
            throw new RegleMetierException("L'e-mail " + req.getEmail() + " est déjà utilisé");
        }
    }

    private Employe mapperRequete(EmployeRequeteDTO req, Employe employe) {
        employe.setMatricule(req.getMatricule());
        employe.setNom(req.getNom().toUpperCase());
        employe.setPrenom(req.getPrenom());
        employe.setEmail(req.getEmail().toLowerCase());
        employe.setTelephone(req.getTelephone());
        employe.setDateNaissance(req.getDateNaissance());
        if (req.getPhotoUrl() != null) employe.setPhotoUrl(req.getPhotoUrl());
        if (req.getStatutEmploi() != null) {
            employe.setStatutEmploi(StatutEmploi.valueOf(req.getStatutEmploi()));
        }
        if (req.getDepartementId() != null) {
            Departement dept = departementRepository.findById(req.getDepartementId())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Département introuvable"));
            employe.setDepartement(dept);
        }
        if (req.getPosteId() != null) {
            Poste poste = posteRepository.findById(req.getPosteId())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Poste introuvable"));
            employe.setPoste(poste);
        }
        return employe;
    }

    private EmployeDTO versDTO(Employe e) {
        boolean aContrat = contratRepository
                .findByEmployeIdAndStatutContratAndStatut(e.getId(), gn.odc.gestionrh.common.enums.StatutContrat.ACTIF, StatutEntite.ACTIF)
                .isPresent();
        var utilisateurOpt = utilisateurRepository.findByEmail(e.getEmail());
        String roleCode = null;
        String roleLibelle = null;
        Boolean compteActif = null;
        Boolean compteConfirme = null;
        if (utilisateurOpt.isPresent()) {
            var u = utilisateurOpt.get();
            compteActif = u.isActif();
            compteConfirme = u.isConfirme();
            if (!u.getRoles().isEmpty()) {
                var role = u.getRoles().iterator().next();
                roleCode = role.getCode();
                roleLibelle = role.getLibelle();
            }
        }
        return EmployeDTO.builder()
                .id(e.getId())
                .matricule(e.getMatricule())
                .nom(e.getNom())
                .prenom(e.getPrenom())
                .email(e.getEmail())
                .telephone(e.getTelephone())
                .dateNaissance(e.getDateNaissance())
                .departementId(e.getDepartement() != null ? e.getDepartement().getId() : null)
                .departementLibelle(e.getDepartement() != null ? e.getDepartement().getLibelle() : null)
                .posteId(e.getPoste() != null ? e.getPoste().getId() : null)
                .posteLibelle(e.getPoste() != null ? e.getPoste().getLibelle() : null)
                .photoUrl(e.getPhotoUrl())
                .statutEmploi(e.getStatutEmploi().name())
                .statut(e.getStatut().name())
                .aContratActif(aContrat)
                .compteActif(compteActif)
                .compteConfirme(compteConfirme)
                .roleCode(roleCode)
                .roleLibelle(roleLibelle)
                .build();
    }
}
