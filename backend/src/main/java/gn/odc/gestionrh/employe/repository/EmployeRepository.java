package gn.odc.gestionrh.employe.repository;

import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.employe.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Optional<Employe> findByEmail(String email);
    Optional<Employe> findByMatricule(String matricule);
    boolean existsByEmail(String email);
    boolean existsByMatricule(String matricule);
    List<Employe> findByStatut(StatutEntite statut);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByMatriculeAndIdNot(String matricule, Long id);
}
