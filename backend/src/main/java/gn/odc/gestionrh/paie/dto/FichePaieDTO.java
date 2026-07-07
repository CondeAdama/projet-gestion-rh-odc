package gn.odc.gestionrh.paie.dto;

import gn.odc.gestionrh.employe.dto.EmployeDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class FichePaieDTO {
    private Long id;
    private Long employeId;
    private EmployeDTO employe;
    private int periodeMois;
    private int periodeAnnee;
    private String periodeLibelle;
    private BigDecimal salaireBrut;
    private BigDecimal cotisationCnss;
    private BigDecimal impotRts;
    private BigDecimal salaireNet;
    private String qrCodeToken;
    private LocalDate dateGeneration;
    private String statut;
}
