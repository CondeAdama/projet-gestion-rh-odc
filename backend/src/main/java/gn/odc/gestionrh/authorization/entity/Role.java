package gn.odc.gestionrh.authorization.entity;

import gn.odc.gestionrh.common.entity.EntiteBase;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends EntiteBase {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean systeme = false;

    /** Rôle attribué automatiquement aux nouveaux employés si aucun rôle n'est précisé */
    @Column(nullable = false)
    private boolean parDefaut = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
