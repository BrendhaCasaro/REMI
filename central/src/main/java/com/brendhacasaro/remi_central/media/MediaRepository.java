package com.brendhacasaro.remi_central.media;

import com.brendhacasaro.remi_central.media.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {
}
