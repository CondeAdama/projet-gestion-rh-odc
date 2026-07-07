package gn.odc.gestionrh.presence.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScanReponseDTO {
    private boolean success;
    private String message;
    private String typeScan;
    private PresenceDTO presence;
}
