package gn.odc.gestionrh.authorization.repository;

import gn.odc.gestionrh.authorization.entity.Role;
import gn.odc.gestionrh.common.enums.StatutEntite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(String code);
    Optional<Role> findByParDefautTrueAndStatut(StatutEntite statut);
    List<Role> findByStatut(StatutEntite statut);
    boolean existsByCode(String code);
}
