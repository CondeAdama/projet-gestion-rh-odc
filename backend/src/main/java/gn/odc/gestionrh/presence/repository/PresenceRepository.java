package gn.odc.gestionrh.presence.repository;

import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.common.enums.StatutPresence;
import gn.odc.gestionrh.presence.entity.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PresenceRepository extends JpaRepository<Presence, Long> {
    Optional<Presence> findFirstByEmployeIdAndDateJourAndStatutAndHeureSortieIsNullOrderByHeureEntreeDesc(
            Long employeId, LocalDate dateJour, StatutEntite statut);
    long countByEmployeIdAndDateJourAndStatut(Long employeId, LocalDate dateJour, StatutEntite statut);
    long countByEmployeIdAndDateJourAndStatutAndHeureEntreeLessThanEqual(
            Long employeId, LocalDate dateJour, StatutEntite statut, java.time.LocalTime heureEntree);
    List<Presence> findByDateJourAndStatut(LocalDate dateJour, StatutEntite statut);
    List<Presence> findByEmployeIdAndStatut(Long employeId, StatutEntite statut);
    List<Presence> findByStatut(StatutEntite statut);
    long countByDateJourAndStatut(LocalDate dateJour, StatutEntite statut);

    @Query("""
        SELECT p FROM Presence p
        JOIN FETCH p.employe e
        JOIN FETCH p.localisation l
        LEFT JOIN e.departement d
        WHERE p.statut = :statutEntite
        AND (:dateDebut IS NULL OR p.dateJour >= :dateDebut)
        AND (:dateFin IS NULL OR p.dateJour <= :dateFin)
        AND (:localisationId IS NULL OR l.id = :localisationId)
        AND (:departementId IS NULL OR d.id = :departementId)
        AND (:statutPresence IS NULL OR p.statutPresence = :statutPresence)
        ORDER BY p.dateJour DESC, p.heureEntree DESC
        """)
    List<Presence> rechercher(
            @Param("statutEntite") StatutEntite statutEntite,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("localisationId") Long localisationId,
            @Param("departementId") Long departementId,
            @Param("statutPresence") StatutPresence statutPresence);
}
