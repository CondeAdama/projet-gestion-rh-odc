package gn.odc.gestionrh.auth.repository;

import gn.odc.gestionrh.auth.entity.Utilisateur;
import gn.odc.gestionrh.common.enums.StatutEntite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Utilisateur> findByEmployeId(Long employeId);
    List<Utilisateur> findByStatut(StatutEntite statut);
}
