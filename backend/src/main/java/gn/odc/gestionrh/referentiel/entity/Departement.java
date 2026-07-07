package gn.odc.gestionrh.referentiel.entity;

import gn.odc.gestionrh.common.entity.EntiteBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "departements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Departement extends EntiteBase {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(length = 500)
    private String description;
}
