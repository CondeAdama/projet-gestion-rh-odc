package gn.odc.gestionrh.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InscriptionRequeteDTO {
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;

    @NotBlank(message = "L'e-mail est obligatoire")
    @Email(message = "Format d'e-mail invalide")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^\\+?[0-9\\s\\-]{8,20}$", message = "Format de téléphone invalide")
    private String telephone;

    @NotBlank(message = "Le matricule est obligatoire")
    @Size(max = 50)
    private String matricule;

    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    private Long departementId;
    private Long posteId;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 100, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Le mot de passe doit contenir au moins une lettre et un chiffre")
    private String motDePasse;
}
