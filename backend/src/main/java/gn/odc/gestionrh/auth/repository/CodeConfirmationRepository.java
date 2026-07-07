package gn.odc.gestionrh.auth.repository;

import gn.odc.gestionrh.auth.entity.CodeConfirmation;
import gn.odc.gestionrh.common.enums.TypeCodeConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodeConfirmationRepository extends JpaRepository<CodeConfirmation, Long> {
    Optional<CodeConfirmation> findTopByEmailAndTypeCodeAndUtiliseFalseOrderByDateCreationDesc(
            String email, TypeCodeConfirmation typeCode);

    Optional<CodeConfirmation> findByTokenAndUtiliseFalseAndTypeCode(
            String token, TypeCodeConfirmation typeCode);
}
