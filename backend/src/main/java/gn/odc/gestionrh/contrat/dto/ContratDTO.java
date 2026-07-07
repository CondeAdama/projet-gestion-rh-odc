package gn.odc.gestionrh.contrat.dto;

import gn.odc.gestionrh.employe.dto.EmployeDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ContratDTO {
    private Long id;
    private Long employeId;
    private EmployeDTO employe;
    private String typeContrat;
    private BigDecimal salaireBase;
    private BigDecimal indemniteTransport;
    private BigDecimal indemniteLogement;
    private BigDecimal autresAvantages;
    private BigDecimal salaireBrut;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statutContrat;
    private String statut;
}
