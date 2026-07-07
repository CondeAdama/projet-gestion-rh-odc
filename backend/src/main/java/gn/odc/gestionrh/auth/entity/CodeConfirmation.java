package gn.odc.gestionrh.auth.entity;

import gn.odc.gestionrh.common.enums.TypeCodeConfirmation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "codes_confirmation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 30)
    private String telephone;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(length = 64)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_code", nullable = false, length = 30)
    private TypeCodeConfirmation typeCode = TypeCodeConfirmation.INSCRIPTION;

    @Column(name = "expire_le", nullable = false)
    private LocalDateTime expireLe;

    @Column(nullable = false)
    private boolean utilise = false;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
}
