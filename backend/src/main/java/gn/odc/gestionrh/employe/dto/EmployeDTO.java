package gn.odc.gestionrh.employe.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EmployeDTO {
    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private LocalDate dateNaissance;
    private Long departementId;
    private String departementLibelle;
    private Long posteId;
    private String posteLibelle;
    private String photoUrl;
    private String statutEmploi;
    private String statut;
    private boolean aContratActif;
    private Boolean compteActif;
    private Boolean compteConfirme;
    private String roleCode;
    private String roleLibelle;
}
