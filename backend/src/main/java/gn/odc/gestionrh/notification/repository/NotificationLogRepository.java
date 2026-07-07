package gn.odc.gestionrh.notification.repository;

import gn.odc.gestionrh.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
}
