package com.example.petbattle;

/** ブラウザから Base64 で画像を送るための JSON ボディ（multipart 拡張を避ける） */
public class CreateCatRequest {

    public String name;
    /** data: プレフィックスなしの Base64 */
    public String imageBase64;
    /** 例: image/jpeg, image/png */
    public String contentType;
}
