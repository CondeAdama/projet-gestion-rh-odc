package gn.odc.gestionrh.contrat.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContratRequeteDTO {
    @NotNull(message = "L'employé est obligatoire")
    private Long employeId;

    @NotBlank(message = "Le type de contrat est obligatoire")
    @Pattern(regexp = "CDI|CDD|STAGE", message = "Type de contrat invalide")
    private String typeContrat;

    @NotNull(message = "Le salaire de base est obligatoire")
    @DecimalMin(value = "0", message = "Le salaire ne peut pas être négatif")
    private BigDecimal salaireBase;

    @DecimalMin(value = "0", message = "L'indemnité ne peut pas être négative")
    private BigDecimal indemniteTransport;

    @DecimalMin(value = "0", message = "L'indemnité ne peut pas être négative")
    private BigDecimal indemniteLogement;

    @DecimalMin(value = "0", message = "Les avantages ne peuvent pas être négatifs")
    private BigDecimal autresAvantages;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    private LocalDate dateFin;
}
