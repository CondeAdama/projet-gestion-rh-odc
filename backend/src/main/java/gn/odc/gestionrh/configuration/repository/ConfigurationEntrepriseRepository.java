package gn.odc.gestionrh.configuration.repository;

import gn.odc.gestionrh.configuration.entity.ConfigurationEntreprise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationEntrepriseRepository extends JpaRepository<ConfigurationEntreprise, Long> {
}
