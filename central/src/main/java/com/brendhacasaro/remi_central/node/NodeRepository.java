package com.brendhacasaro.remi_central.node;

import com.brendhacasaro.remi_central.node.model.Node;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeRepository extends JpaRepository<Node, Integer> {
}
