package com.brendhacasaro.remi_central.node;

import com.brendhacasaro.remi_central.node.model.Node;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NodeRepository extends JpaRepository<Node, Integer> {
    Optional<Node> findTopByStatusOrderByDiskFreeDesc(NodeStatus status);
}
