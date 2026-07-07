package gn.odc.gestionrh.notification.entity;

import gn.odc.gestionrh.common.enums.TypeNotification;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "destinataire_email", length = 150)
    private String destinataireEmail;

    @Column(name = "destinataire_telephone", length = 30)
    private String destinataireTelephone;

    @Column(nullable = false, length = 20)
    private String canal;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_notification", nullable = false, length = 50)
    private TypeNotification typeNotification;

    @Column(length = 255)
    private String sujet;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "statut_envoi", nullable = false, length = 20)
    private String statutEnvoi = "ENVOYE";

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
}
