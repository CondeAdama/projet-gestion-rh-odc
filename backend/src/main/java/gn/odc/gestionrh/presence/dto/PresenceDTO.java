package gn.odc.gestionrh.presence.dto;

import gn.odc.gestionrh.employe.dto.EmployeDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class PresenceDTO {
    private Long id;
    private Long employeId;
    private EmployeDTO employe;
    private Long localisationId;
    private String localisationNom;
    private String localisationVille;
    private LocalDate dateJour;
    private LocalTime heureEntree;
    private LocalTime heureSortie;
    private String statutPresence;
    private String typeScan;
    /** Numéro du passage dans la journée (1 = première entrée, 2 = retour après sortie, …) */
    private Integer numeroPassage;
}
