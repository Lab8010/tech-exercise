package com.example.petbattle;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/cats")
public class CatResource {

    @GET
    public java.util.List<Cat> listAll() {
        return Cat.listAll();
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
        // 例: +10 を +1 に変更すると、同じ投票操作での増え方が変わる。
        int voteStep = 10;
        // ----------------------------------------------------------------------

        if (down) {
            cat.count -= voteStep;
        } else {
            cat.count += voteStep;
        }
        return Response.ok(cat).build();
    }
}
