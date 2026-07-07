package gn.odc.gestionrh.visite.entity;

import gn.odc.gestionrh.common.enums.StatutVisite;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "visites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visiteur_id", nullable = false)
    private Visiteur visiteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carte_visite_id", nullable = false)
    private CarteVisite carteVisite;

    @Column(nullable = false, length = 255)
    private String motif;

    @Column(name = "date_heure_entree", nullable = false)
    private LocalDateTime dateHeureEntree;

    @Column(name = "date_heure_sortie")
    private LocalDateTime dateHeureSortie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutVisite statut = StatutVisite.EN_COURS;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
}
