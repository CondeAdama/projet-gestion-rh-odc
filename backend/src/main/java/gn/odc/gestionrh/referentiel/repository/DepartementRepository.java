package gn.odc.gestionrh.referentiel.repository;

import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.referentiel.entity.Departement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartementRepository extends JpaRepository<Departement, Long> {
    List<Departement> findByStatut(StatutEntite statut);
    Optional<Departement> findByCode(String code);
    boolean existsByCode(String code);
}
