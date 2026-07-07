package gn.odc.gestionrh.contrat.entity;

import gn.odc.gestionrh.common.entity.EntiteBase;
import gn.odc.gestionrh.common.enums.StatutContrat;
import gn.odc.gestionrh.employe.entity.Employe;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contrats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contrat extends EntiteBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(name = "type_contrat", nullable = false, length = 50)
    private String typeContrat;

    @Column(name = "salaire_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal salaireBase;

    @Column(name = "indemnite_transport", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal indemniteTransport = BigDecimal.ZERO;

    @Column(name = "indemnite_logement", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal indemniteLogement = BigDecimal.ZERO;

    @Column(name = "autres_avantages", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal autresAvantages = BigDecimal.ZERO;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_contrat", nullable = false, length = 20)
    @Builder.Default
    private StatutContrat statutContrat = StatutContrat.ACTIF;
}
