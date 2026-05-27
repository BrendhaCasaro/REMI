package com.brendhacasaro.remi_node.media;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NodeMediaFileRepository extends JpaRepository<NodeMediaFile, UUID> {
}
