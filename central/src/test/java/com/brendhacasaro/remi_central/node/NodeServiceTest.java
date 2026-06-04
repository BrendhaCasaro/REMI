package com.brendhacasaro.remi_central.node;

import com.brendhacasaro.remi_central.media.MediaRepository;
import com.brendhacasaro.remi_central.media.model.Media;
import com.brendhacasaro.remi_central.node.dto.NodeConfigRequest;
import com.brendhacasaro.remi_central.node.dto.NodePatchRequest;
import com.brendhacasaro.remi_central.node.model.Node;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NodeServiceTest {

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private NodeService nodeService;

    private final UUID nodeKey = UUID.randomUUID();

    @Test
    void createNode_shouldPersistAndReturn() {
        NodeConfigRequest request = new NodeConfigRequest(
                "http://node1:8080", 100.0, nodeKey, NodeStatus.ONLINE);
        Node saved = new Node("http://node1:8080", 100.0, nodeKey, NodeStatus.ONLINE);
        when(nodeRepository.save(any())).thenReturn(saved);

        Node result = nodeService.createNode(request);

        assertNotNull(result);
        assertEquals("http://node1:8080", result.getUrl());
        assertEquals(100.0, result.getTotalCapacity());
        assertEquals(nodeKey, result.getKey());
        assertEquals(NodeStatus.ONLINE, result.getStatus());
        verify(nodeRepository).save(any());
    }

    @Test
    void patchNode_shouldUpdateAllFields() {
        Node existing = new Node("http://old:8080", 50.0, nodeKey, NodeStatus.OFFLINE);
        when(nodeRepository.findById(1)).thenReturn(Optional.of(existing));
        UUID newKey = UUID.randomUUID();
        NodePatchRequest request = new NodePatchRequest(
                "http://new:8080", 200.0, newKey, NodeStatus.ONLINE);
        when(nodeRepository.save(any())).thenReturn(existing);

        Node result = nodeService.patchNode(1, request);

        assertEquals("http://new:8080", result.getUrl());
        assertEquals(200.0, result.getTotalCapacity());
        assertEquals(newKey, result.getKey());
        assertEquals(NodeStatus.ONLINE, result.getStatus());
    }

    @Test
    void patchNode_shouldIgnoreNullFields() {
        Node existing = new Node("http://keep:8080", 50.0, nodeKey, NodeStatus.ONLINE);
        when(nodeRepository.findById(1)).thenReturn(Optional.of(existing));
        when(nodeRepository.save(any())).thenReturn(existing);

        Node result = nodeService.patchNode(1, new NodePatchRequest(null, null, null, null));

        assertEquals("http://keep:8080", result.getUrl());
        assertEquals(50.0, result.getTotalCapacity());
        assertEquals(nodeKey, result.getKey());
        assertEquals(NodeStatus.ONLINE, result.getStatus());
    }

    @Test
    void patchNode_shouldThrowWhenNotFound() {
        when(nodeRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> nodeService.patchNode(99, new NodePatchRequest(null, null, null, null)));
    }

    @Test
    void getAllNodes_shouldReturnNodes() {
        Node node1 = new Node("http://a:8080", 100.0, nodeKey, NodeStatus.ONLINE);
        Node node2 = new Node("http://b:8080", 200.0, nodeKey, NodeStatus.OFFLINE);
        when(nodeRepository.findAll()).thenReturn(List.of(node1, node2));

        List<Node> nodes = nodeService.getAllNodes();

        assertEquals(2, nodes.size());
        assertEquals("http://a:8080", nodes.get(0).getUrl());
        assertEquals("http://b:8080", nodes.get(1).getUrl());
    }

    @Test
    void deleteNode_shouldDeleteMediaThenNode() {
        Node node = new Node("http://node:8080", 100.0, nodeKey, NodeStatus.ONLINE);
        when(nodeRepository.findById(1)).thenReturn(Optional.of(node));
        when(mediaRepository.findByNodeId(1)).thenReturn(List.of(new Media("test.txt")));

        nodeService.deleteNode(1);

        verify(mediaRepository).deleteAll(any());
        verify(nodeRepository).delete(node);
    }

    @Test
    void deleteNode_shouldDeleteNodeEvenWithoutMedia() {
        Node node = new Node("http://node:8080", 100.0, nodeKey, NodeStatus.ONLINE);
        when(nodeRepository.findById(1)).thenReturn(Optional.of(node));
        when(mediaRepository.findByNodeId(1)).thenReturn(List.of());

        nodeService.deleteNode(1);

        verify(mediaRepository, never()).deleteAll(any());
        verify(nodeRepository).delete(node);
    }

    @Test
    void deleteNode_shouldThrowWhenNotFound() {
        when(nodeRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> nodeService.deleteNode(99));
    }
}
