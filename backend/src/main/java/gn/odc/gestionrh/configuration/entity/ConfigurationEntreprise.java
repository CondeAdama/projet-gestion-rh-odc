package gn.odc.gestionrh.configuration.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuration_entreprise")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurationEntreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_entreprise", nullable = false, length = 150)
    private String nomEntreprise;

    @Column(length = 255)
    private String adresse;

    @Column(length = 30)
    private String telephone;

    @Column(length = 150)
    private String email;

    @Column(length = 50)
    private String nif;

    @Column(name = "numero_cnss", length = 50)
    private String numeroCnss;

    @Column(name = "taux_cnss", precision = 5, scale = 2)
    @Builder.Default
    private java.math.BigDecimal tauxCnss = new java.math.BigDecimal("5.00");

    @Column(name = "taux_rts", precision = 5, scale = 2)
    @Builder.Default
    private java.math.BigDecimal tauxRts = new java.math.BigDecimal("10.00");

    @Column(length = 255)
    private String slogan;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String devise = "GNF";

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    @PreUpdate
    void touch() {
        dateModification = LocalDateTime.now();
    }
}
