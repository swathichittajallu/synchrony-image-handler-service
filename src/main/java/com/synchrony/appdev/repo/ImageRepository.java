package com.synchrony.appdev.repo;

import com.synchrony.appdev.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByName(String name);
    void deleteImageById(@Param("id") Long id);
}
