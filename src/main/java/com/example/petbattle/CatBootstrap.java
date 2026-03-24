package com.example.petbattle;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

/**
 * 起動時に猫データを投入する（import.sql より RDB 方言差の影響を受けにくい）。
 */
@ApplicationScoped
public class CatBootstrap {

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (Cat.count() > 0) {
            return;
        }
        persistCat("Mittens", "/images/cat1.jpg");
        persistCat("Whiskers", "/images/cat2.jpg");
        persistCat("Luna", "/images/cat3.jpg");
    }

    private void persistCat(String name, String imagePath) {
        Cat cat = new Cat();
        cat.name = name;
        cat.count = 0;
        cat.imagePath = imagePath;
        cat.persist();
    }
}
