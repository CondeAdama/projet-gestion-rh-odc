package gn.odc.gestionrh.auth.controller;

import gn.odc.gestionrh.auth.dto.*;
import gn.odc.gestionrh.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthReponseDTO login(@Valid @RequestBody LoginRequeteDTO requete) {
        return authService.login(requete);
    }

    @PostMapping("/inscription")
    public ResponseEntity<Map<String, String>> inscrire(@Valid @RequestBody InscriptionRequeteDTO requete) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authService.inscrire(requete));
    }

    @PostMapping("/activer-compte")
    public AuthReponseDTO activerCompte(@Valid @RequestBody ActiverCompteRequeteDTO requete) {
        return authService.activerCompte(requete);
    }

    @GetMapping("/verifier-token")
    public Map<String, String> verifierToken(@RequestParam String token) {
        return authService.verifierToken(token);
    }

    @PostMapping("/confirmer")
    public AuthReponseDTO confirmer(@Valid @RequestBody ConfirmationRequeteDTO requete) {
        return authService.confirmer(requete);
    }

    @PostMapping("/renvoyer-code")
    public Map<String, String> renvoyerCode(@Valid @RequestBody RenvoyerCodeRequeteDTO requete) {
        return authService.renvoyerCode(requete.getEmail());
    }

    @GetMapping("/moi")
    public ProfilDTO profil(Authentication auth) {
        return authService.profil(auth);
    }

    @PutMapping("/moi")
    public ProfilDTO modifierProfil(Authentication auth, @Valid @RequestBody ProfilModifierDTO dto) {
        return authService.modifierProfil(auth, dto);
    }

    @PutMapping("/moi/mot-de-passe")
    public Map<String, String> modifierMotDePasse(Authentication auth, @Valid @RequestBody MotDePasseModifierDTO dto) {
        return authService.modifierMotDePasse(auth, dto);
    }

    @PostMapping("/mot-de-passe-oublie")
    public Map<String, String> demanderReinitialisation(@Valid @RequestBody DemanderReinitialisationDTO dto) {
        return authService.demanderReinitialisation(dto);
    }

    @GetMapping("/verifier-token-reinitialisation")
    public Map<String, String> verifierTokenReinitialisation(@RequestParam String token) {
        return authService.verifierTokenReinitialisation(token);
    }

    @PostMapping("/reinitialiser-mot-de-passe")
    public Map<String, String> reinitialiserMotDePasse(@Valid @RequestBody ReinitialiserMotDePasseDTO dto) {
        return authService.reinitialiserMotDePasse(dto);
    }

    @PostMapping("/moi/photo")
    public ProfilDTO modifierPhotoProfil(Authentication auth, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        return authService.modifierPhotoProfil(auth, file);
    }
}
