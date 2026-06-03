package com.brendhacasaro.remi_central.node;

import com.brendhacasaro.remi_central.config.TestcontainersConfig;
import com.brendhacasaro.remi_central.node.model.Node;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfig.class)
class NodeRepositoryTest {

    @Autowired
    private NodeRepository nodeRepository;

    private final UUID nodeKey = UUID.randomUUID();

    @Test
    void saveAndFindById() {
        Node node = new Node("http://node:8080", 100.0, nodeKey, NodeStatus.ONLINE);
        node = nodeRepository.save(node);

        Optional<Node> found = nodeRepository.findById(node.getId());

        assertTrue(found.isPresent());
        assertEquals("http://node:8080", found.get().getUrl());
        assertEquals(100.0, found.get().getTotalCapacity());
        assertEquals(NodeStatus.ONLINE, found.get().getStatus());
    }

    @Test
    void findAll_shouldReturnAllNodes() {
        nodeRepository.save(new Node("http://n1:8080", 50.0, nodeKey, NodeStatus.ONLINE));
        nodeRepository.save(new Node("http://n2:8080", 100.0, nodeKey, NodeStatus.OFFLINE));

        List<Node> nodes = nodeRepository.findAll();

        assertEquals(2, nodes.size());
    }

    @Test
    void deleteNode() {
        Node node = nodeRepository.save(
                new Node("http://delete:8080", 50.0, nodeKey, NodeStatus.ONLINE));

        nodeRepository.delete(node);

        assertTrue(nodeRepository.findById(node.getId()).isEmpty());
    }
}
