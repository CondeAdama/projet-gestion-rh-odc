package gn.odc.gestionrh.employe.entity;

import gn.odc.gestionrh.common.entity.EntiteBase;
import gn.odc.gestionrh.common.enums.StatutEmploi;
import gn.odc.gestionrh.referentiel.entity.Departement;
import gn.odc.gestionrh.referentiel.entity.Poste;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employe extends EntiteBase {

    @Column(nullable = false, unique = true, length = 50)
    private String matricule;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 30)
    private String telephone;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    private Departement departement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poste_id")
    private Poste poste;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_emploi", nullable = false, length = 20)
    @Builder.Default
    private StatutEmploi statutEmploi = StatutEmploi.ACTIF;
}
