package com.brendhacasaro.remi_central.node_media;

import com.brendhacasaro.remi_central.node.model.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NodeMediaRepository extends JpaRepository<NodeMedia, Long> {
    @Query("""
                SELECT n FROM NodeMedia nm
                JOIN nm.node n
                WHERE nm.media.id = :mediaId
            """)
    Optional<Node> findNodeByMediaId(UUID mediaId);

    @Modifying
    @Query("""
                DELETE FROM NodeMedia nm
                WHERE nm.media.id = :mediaId
            """)
    void deleteByMediaId(UUID mediaId);

    @Query("""
                SELECT nm.media.id FROM NodeMedia nm
                WHERE nm.node.id = :nodeId
            """)
    List<UUID> findMediaIdsByNodeId(Integer nodeId);

    @Modifying
    @Query("""
                DELETE FROM NodeMedia nm
                WHERE nm.node.id = :nodeId
            """)
    void deleteByNodeId(Integer nodeId);
}
