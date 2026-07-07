package gn.odc.gestionrh.paie.repository;

import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.paie.entity.FichePaie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FichePaieRepository extends JpaRepository<FichePaie, Long> {
    Optional<FichePaie> findByEmployeIdAndPeriodeMoisAndPeriodeAnneeAndStatut(
            Long employeId, int mois, int annee, StatutEntite statut);
    List<FichePaie> findByEmployeIdAndStatut(Long employeId, StatutEntite statut);
    List<FichePaie> findByStatut(StatutEntite statut);
}
