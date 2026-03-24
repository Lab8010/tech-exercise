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
}
