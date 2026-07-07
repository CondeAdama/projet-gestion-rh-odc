package gn.odc.gestionrh.configuration.repository;

import gn.odc.gestionrh.configuration.entity.ConfigurationNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationNotificationRepository extends JpaRepository<ConfigurationNotification, Long> {
}
