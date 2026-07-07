package gn.odc.gestionrh.paie.entity;

import gn.odc.gestionrh.common.entity.EntiteBase;
import gn.odc.gestionrh.employe.entity.Employe;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fiches_paie", uniqueConstraints = @UniqueConstraint(columnNames = {"employe_id", "periode_mois", "periode_annee"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichePaie extends EntiteBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(name = "periode_mois", nullable = false)
    private int periodeMois;

    @Column(name = "periode_annee", nullable = false)
    private int periodeAnnee;

    @Column(name = "salaire_brut", nullable = false, precision = 12, scale = 2)
    private BigDecimal salaireBrut;

    @Column(name = "cotisation_cnss", nullable = false, precision = 12, scale = 2)
    private BigDecimal cotisationCnss;

    @Column(name = "impot_rts", nullable = false, precision = 12, scale = 2)
    private BigDecimal impotRts;

    @Column(name = "salaire_net", nullable = false, precision = 12, scale = 2)
    private BigDecimal salaireNet;

    @Column(name = "qr_code_token", nullable = false, unique = true, length = 255)
    private String qrCodeToken;

    @Column(name = "date_generation", nullable = false)
    private LocalDate dateGeneration;
}
