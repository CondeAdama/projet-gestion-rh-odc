package gn.odc.gestionrh.authorization.entity;

import gn.odc.gestionrh.common.entity.EntiteBase;
import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.TypeAction;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions", uniqueConstraints = @UniqueConstraint(columnNames = {"module", "action"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends EntiteBase {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ModuleApplication module;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeAction action;

    public String getCle() {
        return module.name() + ":" + action.name();
    }
}
