package gn.odc.gestionrh.visite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VisiteurRequeteDTO {
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;

    @NotBlank(message = "Le contact est obligatoire")
    @Pattern(regexp = "^(\\+?[0-9\\s\\-]{8,20}|[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,})$", message = "Contact invalide (téléphone ou e-mail)")
    private String contact;

    @Size(max = 150)
    private String entreprise;
}
