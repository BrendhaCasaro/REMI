package com.brendhacasaro.remi_node.stored_media;

import com.brendhacasaro.remi_node.stored_media.model.StoredMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoredMediaRepository extends JpaRepository<StoredMedia, UUID> {
}
