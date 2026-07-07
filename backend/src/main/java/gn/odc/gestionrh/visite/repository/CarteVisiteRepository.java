package gn.odc.gestionrh.visite.repository;

import gn.odc.gestionrh.common.enums.StatutCarteVisite;
import gn.odc.gestionrh.visite.entity.CarteVisite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarteVisiteRepository extends JpaRepository<CarteVisite, Long> {
    List<CarteVisite> findByStatut(StatutCarteVisite statut);
    Optional<CarteVisite> findByNumeroCarte(String numeroCarte);
}
