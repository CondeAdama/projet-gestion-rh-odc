package gn.odc.gestionrh.common.util;

import gn.odc.gestionrh.common.exception.RegleMetierException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public final class ValidateurFichier {

    private static final long MAX_IMAGE_BYTES = 2 * 1024 * 1024;
    private static final Set<String> EXTENSIONS_AUTORISEES = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final Set<String> TYPES_AUTORISES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private ValidateurFichier() {}

    public static void validerImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RegleMetierException("Le fichier est obligatoire");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new RegleMetierException("Le fichier ne doit pas dépasser 2 Mo");
        }
        String ext = extraireExtension(file.getOriginalFilename());
        if (!EXTENSIONS_AUTORISEES.contains(ext)) {
            throw new RegleMetierException("Format non autorisé. Utilisez JPG, PNG, WEBP ou GIF");
        }
        String contentType = file.getContentType();
        if (contentType != null && !TYPES_AUTORISES.contains(contentType.toLowerCase())) {
            throw new RegleMetierException("Type de fichier non autorisé");
        }
    }

    public static String extraireExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }
}
