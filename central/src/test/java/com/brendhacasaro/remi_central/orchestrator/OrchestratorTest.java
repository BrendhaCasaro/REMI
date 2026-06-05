package com.brendhacasaro.remi_central.orchestrator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.brendhacasaro.remi_central.node.NodeRepository;
import com.brendhacasaro.remi_central.node.NodeStatus;
import com.brendhacasaro.remi_central.node.model.Node;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrchestratorTest {

    @Mock
    private NodeRepository nodeRepository;

    @InjectMocks
    private Orchestrator orchestrator;

    private final String nodeKey = "test-key";

    @Test
    void chooseNode_shouldThrowWhenNoNodesOnline() {
        when(nodeRepository.findTopByStatusOrderByDiskFreeDesc(NodeStatus.ONLINE))
                .thenReturn(Optional.empty());

        assertThrows(OrchestratorException.class, () ->
                orchestrator.chooseNode()
        );
    }

    @Test
    void chooseNode_shouldReturnTheOnlyOnlineNode() {
        Node node = new Node("http://node1:8080", 100.0, nodeKey, NodeStatus.ONLINE, 50.0);
        when(nodeRepository.findTopByStatusOrderByDiskFreeDesc(NodeStatus.ONLINE))
                .thenReturn(Optional.of(node));

        Node result = orchestrator.chooseNode();

        assertNotNull(result);
        assertEquals("http://node1:8080", result.getUrl());
    }

    @Test
    void chooseNode_shouldPickNodeWithMostFreeDisk() {
        Node node = new Node("http://best:8080", 200.0, nodeKey, NodeStatus.ONLINE, 100.0);
        when(nodeRepository.findTopByStatusOrderByDiskFreeDesc(NodeStatus.ONLINE))
                .thenReturn(Optional.of(node));

        Node result = orchestrator.chooseNode();

        assertEquals("http://best:8080", result.getUrl());
    }
}
