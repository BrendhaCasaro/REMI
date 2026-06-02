package com.brendhacasaro.remi_central.orchestrator;

import com.brendhacasaro.remi_central.node.NodeRepository;
import com.brendhacasaro.remi_central.node.NodeStatus;
import com.brendhacasaro.remi_central.node.model.Node;
import com.brendhacasaro.remi_central.orchestrator.dto.MetricsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrchestratorTest {

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private Orchestrator orchestrator;

    private final UUID nodeKey = UUID.randomUUID();

    @Test
    void chooseNode_shouldThrowWhenNoNodesAvailable() {
        when(nodeRepository.findAll()).thenReturn(List.of());

        assertThrows(OrchestratorException.class, () -> orchestrator.chooseNode());
    }

    @Test
    void chooseNode_shouldThrowWhenAllNodesUnhealthy() {
        Node node = new Node("http://node1:8080", 100.0, nodeKey, NodeStatus.ONLINE);
        when(nodeRepository.findAll()).thenReturn(List.of(node));
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchange(any())).thenReturn(false);

        assertThrows(OrchestratorException.class, () -> orchestrator.chooseNode());
    }

    @Test
    void chooseNode_shouldReturnNodeWhenSingleHealthyNode() {
        Node node = new Node("http://node1:8080", 100.0, nodeKey, NodeStatus.ONLINE);
        when(nodeRepository.findAll()).thenReturn(List.of(node));
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);

        // First call is health check (exchange), second is metrics (retrieve)
        when(requestHeadersSpec.exchange(any())).thenReturn(true);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(MetricsResponse.class)).thenReturn(new MetricsResponse(50.0));

        Node result = orchestrator.chooseNode();

        assertNotNull(result);
        assertEquals("http://node1:8080", result.getUrl());
    }

    @Test
    void chooseNode_shouldPickNodeWithMostFreeDisk() {
        Node node1 = new Node("http://node1:8080", 100.0, nodeKey, NodeStatus.ONLINE);
        Node node2 = new Node("http://node2:8080", 200.0, nodeKey, NodeStatus.ONLINE);
        Node node3 = new Node("http://node3:8080", 50.0, nodeKey, NodeStatus.ONLINE);
        when(nodeRepository.findAll()).thenReturn(List.of(node1, node2, node3));
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchange(any())).thenReturn(true);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        // node1: 10% used, node2: 80% used, node3: 30% used → node1 has most free
        when(responseSpec.body(MetricsResponse.class))
                .thenReturn(new MetricsResponse(10.0))
                .thenReturn(new MetricsResponse(80.0))
                .thenReturn(new MetricsResponse(30.0));

        Node result = orchestrator.chooseNode();

        assertEquals("http://node1:8080", result.getUrl());
    }
}
