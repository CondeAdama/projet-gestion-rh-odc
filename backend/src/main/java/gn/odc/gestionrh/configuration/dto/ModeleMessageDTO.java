package gn.odc.gestionrh.configuration.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModeleMessageDTO {
    @Size(max = 200)
    private String emailSujet;

    @Size(max = 4000)
    private String emailCorps;

    @Size(max = 500)
    private String smsCorps;
}
