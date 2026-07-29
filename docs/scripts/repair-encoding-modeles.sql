-- Réparation manuelle des modèles de messages (Aiven / prod)
-- Cause : import SQL ou connexion sans UTF-8 → accents remplacés par « ?? »
--
-- Option A — réinitialiser depuis les défauts Java (recommandé après déploiement du correctif) :
--   POST /api/v2/configuration/notifications/reinitialiser-modeles  (admin JWT)
--
-- Option B — vider le JSON corrompu (au prochain démarrage, fusion avec défauts côté API) :
UPDATE configuration_notifications
SET modeles_messages = NULL
WHERE modeles_messages LIKE '%??%'
   OR modeles_messages LIKE '%├%'
   OR modeles_messages LIKE '%Ã%';

-- Option C — forcer une réécriture si vous avez sauvegardé un JSON UTF-8 valide :
-- UPDATE configuration_notifications SET modeles_messages = '<json utf8mb4>' WHERE id = 1;

-- Vérification :
-- SELECT id, LEFT(modeles_messages, 200) FROM configuration_notifications;
