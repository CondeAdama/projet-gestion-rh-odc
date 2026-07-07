package gn.odc.gestionrh.visite.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarteVisiteDTO {
    private Long id;
    private String numeroCarte;
    private String statut;
}
