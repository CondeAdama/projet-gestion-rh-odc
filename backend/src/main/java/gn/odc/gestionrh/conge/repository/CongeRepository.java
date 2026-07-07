package gn.odc.gestionrh.conge.repository;

import gn.odc.gestionrh.common.enums.StatutConge;
import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.conge.entity.Conge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CongeRepository extends JpaRepository<Conge, Long> {
    List<Conge> findByStatut(StatutEntite statut);
    List<Conge> findByEmployeIdAndStatut(Long employeId, StatutEntite statut);
    List<Conge> findByStatutCongeAndStatut(StatutConge statutConge, StatutEntite statut);
    long countByStatutCongeAndStatut(StatutConge statutConge, StatutEntite statut);
}
