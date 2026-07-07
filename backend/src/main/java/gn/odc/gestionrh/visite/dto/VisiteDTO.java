package gn.odc.gestionrh.visite.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class VisiteDTO {
    private Long id;
    private Long visiteurId;
    private VisiteurDTO visiteur;
    private Long carteVisiteId;
    private String numeroCarte;
    private String motif;
    private LocalDate dateJour;
    private LocalDateTime dateHeureEntree;
    private LocalDateTime dateHeureSortie;
    private LocalTime heureEntree;
    private LocalTime heureSortie;
    private String statut;
}
