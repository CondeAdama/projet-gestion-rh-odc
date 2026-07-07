package gn.odc.gestionrh.contrat.repository;

import gn.odc.gestionrh.common.enums.StatutContrat;
import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.contrat.entity.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContratRepository extends JpaRepository<Contrat, Long> {
    List<Contrat> findByStatut(StatutEntite statut);
    long countByStatutContratAndStatut(StatutContrat statutContrat, StatutEntite statut);
    Optional<Contrat> findByEmployeIdAndStatutContratAndStatut(Long employeId, StatutContrat statutContrat, StatutEntite statut);
    List<Contrat> findByEmployeIdAndStatut(Long employeId, StatutEntite statut);
}
