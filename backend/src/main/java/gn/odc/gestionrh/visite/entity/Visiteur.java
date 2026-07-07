package gn.odc.gestionrh.visite.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "visiteurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visiteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, length = 50)
    private String contact;

    @Column(length = 150)
    private String entreprise;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String statut = "ACTIF";

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
}
