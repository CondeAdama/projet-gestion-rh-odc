package gn.odc.gestionrh.auth.entity;

import gn.odc.gestionrh.authorization.entity.Role;
import gn.odc.gestionrh.common.entity.EntiteBase;
import gn.odc.gestionrh.employe.entity.Employe;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "utilisateurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur extends EntiteBase {

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", unique = true)
    private Employe employe;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(nullable = false)
    private boolean confirme = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "utilisateur_roles",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
