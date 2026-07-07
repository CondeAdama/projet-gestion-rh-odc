package gn.odc.gestionrh.referentiel.entity;

import gn.odc.gestionrh.common.entity.EntiteBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "localisations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Localisation extends EntiteBase {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 255)
    private String adresse;

    @Column(nullable = false, length = 100)
    private String ville = "Conakry";
}
