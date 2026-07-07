package gn.odc.gestionrh.conge.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CongeRequeteDTO {
    private Long employeId;

    @NotBlank(message = "Le type de congé est obligatoire")
    @Pattern(regexp = "ANNUEL|PAYE|MALADIE|MATERNITE|SANS_SOLDE", message = "Type de congé invalide")
    private String typeConge;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;

    @Size(max = 500, message = "Le motif ne doit pas dépasser 500 caractères")
    private String motif;
}
