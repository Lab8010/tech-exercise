package com.example.petbattle;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "cat")
public class Cat extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public int count;

    /** ブラウザから参照する静的画像パス（例: {@code /images/cat1.jpg}） */
    @Column(name = "image_path", nullable = false)
    public String imagePath;
}
