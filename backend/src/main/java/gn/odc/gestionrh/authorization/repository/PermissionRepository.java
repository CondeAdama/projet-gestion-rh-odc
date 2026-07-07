package gn.odc.gestionrh.authorization.repository;

import gn.odc.gestionrh.authorization.entity.Permission;
import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.TypeAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByModuleAndAction(ModuleApplication module, TypeAction action);
    boolean existsByModuleAndAction(ModuleApplication module, TypeAction action);
}
