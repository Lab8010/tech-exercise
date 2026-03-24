package com.example.petbattle;

import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/cats")
public class CatResource {

    @Inject
    UploadStorage uploadStorage;

    @ConfigProperty(name = "petbattle.upload.max-bytes", defaultValue = "15728640")
    int maxUploadBytes;

    @GET
    public java.util.List<Cat> listAll() {
        return Cat.listAll(Sort.by("count", Sort.Direction.Descending).and("id", Sort.Direction.Ascending));
    }

    /**
     * 名前と Base64 画像を受け取り、新しい猫を追加する。
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response create(CreateCatRequest body) throws IOException {
        if (body == null || body.name == null || body.name.isBlank()) {
            return badRequest("name is required");
        }
        if (body.imageBase64 == null || body.imageBase64.isBlank()) {
            return badRequest("imageBase64 is required");
        }
        String ct = body.contentType != null ? body.contentType.toLowerCase() : "";
        if (!ct.startsWith("image/")) {
            return badRequest("contentType must be an image/* type");
        }

        byte[] raw;
        try {
            raw = Base64.getDecoder().decode(sanitizeBase64(body.imageBase64));
        } catch (IllegalArgumentException e) {
            return badRequest("invalid base64");
        }
        if (raw.length == 0) {
            return badRequest("empty image");
        }
        if (raw.length > maxUploadBytes) {
            return badRequest("image too large");
        }
        if (!looksLikeImage(raw, ct)) {
            return badRequest("file does not look like a valid image");
        }

        String ext = extensionFromContentType(ct);
        String stored = UUID.randomUUID() + "." + ext;
        java.nio.file.Path dest = uploadStorage.getRoot().resolve(stored);
        Files.write(dest, raw);

        Cat cat = new Cat();
        cat.name = body.name.trim();
        cat.count = 0;
        cat.imagePath = "/uploads/" + stored;
        cat.persist();
        return Response.ok(cat).build();
    }

    private static Response badRequest(String msg) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity("{\"error\":\"" + escapeJson(msg) + "\"}")
                .build();
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String sanitizeBase64(String s) {
        String t = s.trim();
        int comma = t.indexOf(',');
        if (t.startsWith("data:") && comma > 0) {
            return t.substring(comma + 1);
        }
        return t;
    }

    private static boolean looksLikeImage(byte[] raw, String contentType) {
        if (raw.length < 8) {
            return false;
        }
        if (contentType.contains("png") && raw[0] == (byte) 0x89 && raw[1] == 'P' && raw[2] == 'N' && raw[3] == 'G') {
            return true;
        }
        if ((contentType.contains("jpeg") || contentType.contains("jpg"))
                && (raw[0] & 0xff) == 0xff
                && (raw[1] & 0xff) == 0xd8) {
            return true;
        }
        if (contentType.contains("gif")
                && raw[0] == 'G'
                && raw[1] == 'I'
                && raw[2] == 'F'
                && raw[3] == '8'
                && (raw[4] == '7' || raw[4] == '9')) {
            return true;
        }
        if (contentType.contains("webp")
                && raw.length >= 12
                && raw[0] == 'R'
                && raw[1] == 'I'
                && raw[2] == 'F'
                && raw[3] == 'F'
                && raw[8] == 'W'
                && raw[9] == 'E'
                && raw[10] == 'B'
                && raw[11] == 'P') {
            return true;
        }
        return false;
    }

    private static String extensionFromContentType(String contentType) {
        if (contentType.contains("png")) {
            return "png";
        }
        if (contentType.contains("gif")) {
            return "gif";
        }
        if (contentType.contains("webp")) {
            return "webp";
        }
        return "jpg";
    }

    /**
     * 投票を受け付け、猫の得票数を更新する。
     * <p>
     * クエリ {@code down=true} のときはマイナス票として扱う（演習用の単純な +/- 切り替え）。
     */
    @POST
    @Path("/{id}/vote")
    @Transactional
    public Response vote(
            @PathParam("id") Long id,
            @DefaultValue("false") @QueryParam("down") boolean down) {
        Cat cat = Cat.findById(id);
        if (cat == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // --- 演習用ホットスポット ---------------------------------------------
        // Jenkins パイプラインでビルドし直したあと、増分が変わることを確認してみよう。
        // 例: voteStep を変更すると、同じ投票操作での増え方が変わる。
        int voteStep = 1;
        // ----------------------------------------------------------------------

        if (down) {
            cat.count -= voteStep;
        } else {
            cat.count += voteStep;
        }
        return Response.ok(cat).build();
    }
}
