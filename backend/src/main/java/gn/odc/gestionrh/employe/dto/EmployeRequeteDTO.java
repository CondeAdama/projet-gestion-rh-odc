package gn.odc.gestionrh.employe.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeRequeteDTO {
    @NotBlank(message = "Le matricule est obligatoire")
    @Size(max = 50, message = "Le matricule ne doit pas dépasser 50 caractères")
    private String matricule;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
    private String prenom;

    @NotBlank(message = "L'e-mail est obligatoire")
    @Email(message = "Format d'e-mail invalide")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^\\+?[0-9\\s\\-]{8,20}$", message = "Format de téléphone invalide")
    private String telephone;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    @NotNull(message = "Le département est obligatoire")
    private Long departementId;

    @NotNull(message = "Le poste est obligatoire")
    private Long posteId;

    @Size(max = 255)
    private String photoUrl;

    @Pattern(regexp = "ACTIF|SUSPENDU|LICENCIE", message = "Statut emploi invalide")
    private String statutEmploi;

    @Size(max = 50)
    private String roleCode;
}
