package gn.odc.gestionrh.referentiel.repository;

import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.referentiel.entity.Localisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocalisationRepository extends JpaRepository<Localisation, Long> {
    List<Localisation> findByStatut(StatutEntite statut);
    Optional<Localisation> findByCode(String code);
    boolean existsByCode(String code);
}
