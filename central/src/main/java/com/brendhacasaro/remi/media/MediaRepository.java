package com.brendhacasaro.remi.media;

import com.brendhacasaro.remi.media.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {
}
