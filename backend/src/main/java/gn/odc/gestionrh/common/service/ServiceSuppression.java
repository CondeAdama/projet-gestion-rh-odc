package gn.odc.gestionrh.common.service;

import gn.odc.gestionrh.common.entity.EntiteBase;
import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.common.exception.RessourceNonTrouveeException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.function.Supplier;

public final class ServiceSuppression {

    private ServiceSuppression() {}

    public static <T extends EntiteBase> void supprimerLogique(T entite) {
        entite.setStatut(StatutEntite.SUPPRIME);
    }

    public static <T extends EntiteBase> void restaurer(T entite) {
        entite.setStatut(StatutEntite.ACTIF);
    }

    public static <T extends EntiteBase> T trouverActif(JpaRepository<T, Long> repo, Long id, String nom) {
        return repo.findById(id)
                .filter(e -> e.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException(nom + " introuvable avec l'id : " + id));
    }

    public static <T extends EntiteBase> T trouverSupprime(JpaRepository<T, Long> repo, Long id, String nom) {
        return repo.findById(id)
                .filter(e -> e.getStatut() == StatutEntite.SUPPRIME)
                .orElseThrow(() -> new RessourceNonTrouveeException(nom + " supprimé introuvable avec l'id : " + id));
    }

    public static <T extends EntiteBase> List<T> listerActifs(Supplier<List<T>> supplier) {
        return supplier.get().stream()
                .filter(e -> e.getStatut() == StatutEntite.ACTIF)
                .toList();
    }
}
