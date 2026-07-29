package gn.odc.gestionrh.common.util;

/**
 * Canonicalise l'URL du frontend (Netlify) et détecte l'ancien hébergement statique Render.
 */
public final class AppUrlNormalizer {

    private static final String LEGACY_RENDER_FRONTEND_HOST = "minerva-rh.onrender.com";

    private AppUrlNormalizer() {
    }

    public static boolean isLegacyRenderFrontend(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String lower = url.toLowerCase();
        return lower.contains(LEGACY_RENDER_FRONTEND_HOST) && !lower.contains("minerva-rh-api");
    }

    public static String trimTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return url.trim().replaceAll("/+$", "");
    }

    public static String resolveFrontendUrl(String storedInDb, String envDefault) {
        String env = trimTrailingSlash(envDefault);
        if (env.isEmpty()) {
            env = "https://minerva-rh.netlify.app";
        }
        if (storedInDb == null || storedInDb.isBlank()) {
            return env;
        }
        String stored = trimTrailingSlash(storedInDb);
        if (isLegacyRenderFrontend(stored)) {
            return env;
        }
        return stored;
    }

    public static String replaceLegacyFrontendUrls(String text, String canonicalBase) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String canon = trimTrailingSlash(canonicalBase);
        return text
                .replace("https://minerva-rh.onrender.com", canon)
                .replace("http://minerva-rh.onrender.com", canon);
    }
}
