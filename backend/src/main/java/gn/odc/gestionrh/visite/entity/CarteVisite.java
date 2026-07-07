package gn.odc.gestionrh.visite.entity;

import gn.odc.gestionrh.common.enums.StatutCarteVisite;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cartes_visite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarteVisite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_carte", nullable = false, unique = true, length = 50)
    private String numeroCarte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutCarteVisite statut = StatutCarteVisite.DISPONIBLE;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
}
