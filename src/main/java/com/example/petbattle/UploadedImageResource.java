package com.example.petbattle;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;

@Path("/uploads")
public class UploadedImageResource {

    @Inject
    UploadStorage uploadStorage;

    @GET
    @Path("/{filename:.+}")
    public Response get(@PathParam("filename") String filename) {
        if (filename == null || filename.isBlank() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new NotFoundException();
        }
        java.nio.file.Path base = uploadStorage.getRoot();
        java.nio.file.Path file = base.resolve(filename).normalize();
        if (!file.startsWith(base) || !Files.isRegularFile(file)) {
            throw new NotFoundException();
        }
        String probe;
        try {
            probe = Files.probeContentType(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        String type = probe != null ? probe : "application/octet-stream";
        InputStream in;
        try {
            in = Files.newInputStream(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return Response.ok(in).type(type).build();
    }
}
