package gn.odc.gestionrh.referentiel.repository;

import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.referentiel.entity.Poste;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PosteRepository extends JpaRepository<Poste, Long> {
    List<Poste> findByStatut(StatutEntite statut);
    Optional<Poste> findByCode(String code);
    boolean existsByCode(String code);
}
