package gn.odc.gestionrh.visite.repository;

import gn.odc.gestionrh.visite.entity.Visiteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisiteurRepository extends JpaRepository<Visiteur, Long> {
    List<Visiteur> findByStatut(String statut);
}
