package gn.odc.gestionrh.referentiel.entity;

import gn.odc.gestionrh.common.entity.EntiteBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "postes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Poste extends EntiteBase {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String libelle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    private Departement departement;

    @Column(length = 500)
    private String description;
}
