package gn.odc.gestionrh.auth.security;

import gn.odc.gestionrh.authorization.entity.Permission;
import gn.odc.gestionrh.authorization.entity.Role;
import gn.odc.gestionrh.auth.entity.Utilisateur;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
public class UtilisateurPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String motDePasse;
    private final boolean actif;
    private final boolean confirme;
    private final Long employeId;
    private final Collection<? extends GrantedAuthority> authorities;

    public UtilisateurPrincipal(Utilisateur utilisateur) {
        this.id = utilisateur.getId();
        this.email = utilisateur.getEmail();
        this.motDePasse = utilisateur.getMotDePasse();
        this.actif = utilisateur.isActif();
        this.confirme = utilisateur.isConfirme();
        this.employeId = utilisateur.getEmploye() != null ? utilisateur.getEmploye().getId() : null;
        this.authorities = construireAuthorities(utilisateur.getRoles());
    }

    private Set<GrantedAuthority> construireAuthorities(Set<Role> roles) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            for (Permission perm : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(perm.getCle()));
            }
        }
        return authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return actif;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return actif && confirme;
    }
}
