package com.brendhacasaro.remi_central.node;

import com.brendhacasaro.remi_central.node.model.Node;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NodeRepository extends JpaRepository<Node, Integer> {
    List<Node> findByStatus(NodeStatus status);
    Optional<Node> findTopByStatusOrderByDiskFreeDesc(NodeStatus status);
}
