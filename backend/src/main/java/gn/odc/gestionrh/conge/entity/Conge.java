package gn.odc.gestionrh.conge.entity;

import gn.odc.gestionrh.common.entity.EntiteBase;
import gn.odc.gestionrh.common.enums.StatutConge;
import gn.odc.gestionrh.common.enums.TypeConge;
import gn.odc.gestionrh.employe.entity.Employe;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "conges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conge extends EntiteBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_conge", nullable = false, length = 50)
    private TypeConge typeConge;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(length = 255)
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_conge", nullable = false, length = 20)
    @Builder.Default
    private StatutConge statutConge = StatutConge.EN_ATTENTE;

    @Column(name = "commentaire_rh", length = 255)
    private String commentaireRh;
}
