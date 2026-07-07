package gn.odc.gestionrh.visite.repository;

import gn.odc.gestionrh.common.enums.StatutVisite;
import gn.odc.gestionrh.visite.entity.Visite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisiteRepository extends JpaRepository<Visite, Long> {
    List<Visite> findByStatut(StatutVisite statut);
    long countByStatut(StatutVisite statut);
}
