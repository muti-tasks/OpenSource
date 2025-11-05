package org.igorgames;

import java.io.IOException;
import java.nio.file.*;

public class makefolder {
    public static void makefolder(String path, String name) {
        Path folder = Path.of(path, name);

        try {
            if (Files.notExists(folder)) {
                Files.createDirectory(folder);
            }
        } catch (IOException e) {
            System.err.println("Failed to create folder: " + e.getMessage());
        }
    }
}
