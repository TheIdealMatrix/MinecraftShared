package de.kevin_stefan.minecraft.shared.gradle;

import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

class MarkerReader {

    private MarkerReader() {
    }

    /**
     * Reads the shared-libraries marker from a directory or a jar; empty if there is none.
     */
    static List<String> read(File file, Logger logger) {
        try {
            if (file.isDirectory()) {
                Path marker = file.toPath().resolve(SharedPlugin.MARKER);
                return Files.isRegularFile(marker) ? parse(Files.readString(marker, StandardCharsets.UTF_8)) : List.of();
            }
            if (file.isFile() && file.getName().endsWith(".jar")) {
                try (JarFile jar = new JarFile(file)) {
                    ZipEntry entry = jar.getEntry(SharedPlugin.MARKER);
                    if (entry == null) {
                        return List.of();
                    }
                    try (InputStream in = jar.getInputStream(entry)) {
                        return parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("sharedLibrary: could not read {}", file, e);
        }
        return List.of();
    }

    private static List<String> parse(String content) {
        return content.lines().map(String::trim).filter(s -> !s.isEmpty() && !s.startsWith("#")).toList();
    }
}
