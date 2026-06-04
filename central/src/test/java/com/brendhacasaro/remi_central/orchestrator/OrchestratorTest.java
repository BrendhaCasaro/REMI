package com.brendhacasaro.remi_central.orchestrator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.brendhacasaro.remi_central.node.NodeRepository;
import com.brendhacasaro.remi_central.node.NodeStatus;
import com.brendhacasaro.remi_central.node.model.Node;
import com.brendhacasaro.remi_central.orchestrator.dto.MetricsResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class OrchestratorTest {

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<RestClient.RequestBodySpec> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private Orchestrator orchestrator;

    private final UUID nodeKey = UUID.randomUUID();

    @Test
    void chooseNode_shouldThrowWhenNoNodesAvailable() {
        when(nodeRepository.findAll()).thenReturn(List.of());

        assertThrows(OrchestratorException.class, () ->
            orchestrator.chooseNode()
        );
    }

    @Test
    void chooseNode_shouldThrowWhenAllNodesUnhealthy() {
        Node node = new Node(
            "http://node1:8080",
            100.0,
            nodeKey,
            NodeStatus.ONLINE
        );
        when(nodeRepository.findAll()).thenReturn(List.of(node));
        doReturn(requestHeadersUriSpec).when(restClient).get();
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(MetricsResponse.class)).thenThrow(new RuntimeException("node down"));

        assertThrows(OrchestratorException.class, () ->
            orchestrator.chooseNode()
        );
    }

    @Test
    void chooseNode_shouldReturnNodeWhenSingleHealthyNode() {
        Node node = new Node(
            "http://node1:8080",
            100.0,
            nodeKey,
            NodeStatus.ONLINE
        );
        when(nodeRepository.findAll()).thenReturn(List.of(node));
        doReturn(requestHeadersUriSpec).when(restClient).get();
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(MetricsResponse.class)).thenReturn(
            new MetricsResponse(50.0)
        );

        Node result = orchestrator.chooseNode();

        assertNotNull(result);
        assertEquals("http://node1:8080", result.getUrl());
    }

    @Test
    void chooseNode_shouldPickNodeWithMostFreeDisk_real() {
        Node node1 = new Node(
            "http://node1:8080",
            100.0,
            nodeKey,
            NodeStatus.ONLINE
        );
        Node node2 = new Node(
            "http://node2:8080",
            200.0,
            nodeKey,
            NodeStatus.ONLINE
        );
        Node node3 = new Node(
            "http://node3:8080",
            50.0,
            nodeKey,
            NodeStatus.ONLINE
        );
        when(nodeRepository.findAll()).thenReturn(List.of(node1, node2, node3));
        doReturn(requestHeadersUriSpec).when(restClient).get();
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // node1: 100GB free, node2: 20GB free, node3: 50GB free → node1 wins
        when(responseSpec.body(MetricsResponse.class))
            .thenReturn(new MetricsResponse(100.0))
            .thenReturn(new MetricsResponse(20.0))
            .thenReturn(new MetricsResponse(50.0));

        Node result = orchestrator.chooseNode();

        assertEquals("http://node1:8080", result.getUrl());
    }
}
