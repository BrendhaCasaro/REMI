package com.brendhacasaro.digital_media.media.repository;

import com.brendhacasaro.digital_media.media.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {}
