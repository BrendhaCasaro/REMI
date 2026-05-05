package com.brendhacasaro.digital_media.node_media;

import com.brendhacasaro.digital_media.node.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface NodeMediaRepository extends JpaRepository<NodeMedia, Long> {
    @Query("""
    SELECT n FROM NodeMedia nm
    JOIN nm.node n
    WHERE nm.media.id = :mediaId
""")
    Optional<Node> findNodeByMediaId(UUID mediaId);
}
