package gn.odc.gestionrh.conge.dto;

import gn.odc.gestionrh.employe.dto.EmployeDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CongeDTO {
    private Long id;
    private Long employeId;
    private EmployeDTO employe;
    private String typeConge;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int nombreJours;
    private String motif;
    private String statutConge;
    private String commentaireRh;
    private String statut;
    private LocalDate dateCreation;
}
