package gn.odc.gestionrh.presence.entity;

import gn.odc.gestionrh.common.entity.EntiteBase;
import gn.odc.gestionrh.common.enums.StatutPresence;
import gn.odc.gestionrh.employe.entity.Employe;
import gn.odc.gestionrh.referentiel.entity.Localisation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "presences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Presence extends EntiteBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localisation_id", nullable = false)
    private Localisation localisation;

    @Column(name = "date_jour", nullable = false)
    private LocalDate dateJour;

    @Column(name = "heure_entree", nullable = false)
    private LocalTime heureEntree;

    @Column(name = "heure_sortie")
    private LocalTime heureSortie;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_presence", nullable = false, length = 20)
    @Builder.Default
    private StatutPresence statutPresence = StatutPresence.EN_REGLE;
}
