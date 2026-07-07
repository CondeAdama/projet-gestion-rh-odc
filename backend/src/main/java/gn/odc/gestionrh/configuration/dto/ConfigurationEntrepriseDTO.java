package gn.odc.gestionrh.configuration.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ConfigurationEntrepriseDTO {
    private Long id;

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Size(max = 150)
    private String nomEntreprise;

    @Size(max = 255)
    private String adresse;

    @Pattern(regexp = "^\\+?[0-9\\s\\-]{8,20}$", message = "Format de téléphone invalide")
    private String telephone;

    @Email(message = "Format d'e-mail invalide")
    @Size(max = 150)
    private String email;

    @Size(max = 50)
    private String nif;

    @Size(max = 50)
    private String numeroCnss;

    @DecimalMin(value = "0", message = "Le taux CNSS doit être positif")
    @DecimalMax(value = "100", message = "Le taux CNSS ne peut dépasser 100%")
    private BigDecimal tauxCnss;

    @DecimalMin(value = "0", message = "Le taux RTS doit être positif")
    @DecimalMax(value = "100", message = "Le taux RTS ne peut dépasser 100%")
    private BigDecimal tauxRts;

    @Size(max = 255)
    private String slogan;

    @Size(max = 255)
    private String logoUrl;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Devise invalide (3 lettres majuscules)")
    private String devise;
}
