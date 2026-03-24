package com.example.petbattle;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class UploadStorage {

    @ConfigProperty(name = "petbattle.upload.dir", defaultValue = "uploads")
    String uploadDir;

    private volatile Path root;

    public Path getRoot() {
        Path r = root;
        if (r == null) {
            synchronized (this) {
                if (root == null) {
                    Path resolved = Paths.get(uploadDir).toAbsolutePath().normalize();
                    try {
                        Files.createDirectories(resolved);
                    } catch (IOException e) {
                        throw new RuntimeException("Cannot create upload dir: " + resolved, e);
                    }
                    root = resolved;
                }
                r = root;
            }
        }
        return r;
    }
}
