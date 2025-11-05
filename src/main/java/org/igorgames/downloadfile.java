package org.igorgames;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;

public class downloadfile {
    public static void downloadFile(String path, String url, String name) {
        Path targetFile = Path.of(path, name);

        try {
            if (Files.notExists(targetFile)) {
                try (InputStream in = URI.create(url).toURL().openStream()) {
                    Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
