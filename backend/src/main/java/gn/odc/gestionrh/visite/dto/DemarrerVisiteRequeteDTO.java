package gn.odc.gestionrh.visite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DemarrerVisiteRequeteDTO {
    @NotNull
    private Long visiteurId;

    @NotNull
    private Long carteId;

    @NotBlank
    @Size(max = 255)
    private String motif;
}
