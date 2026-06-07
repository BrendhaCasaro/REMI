package com.brendhacasaro.remi_central.scheduler;

import com.brendhacasaro.remi_central.node.NodeRepository;
import com.brendhacasaro.remi_central.node.NodeStatus;
import com.brendhacasaro.remi_central.node.model.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NodeHealthSchedulerTest {

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private RestClient restClient;

    private NodeHealthScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new NodeHealthScheduler(nodeRepository, restClient);
    }

    @Test
    void checkHealth_shouldSetNodeOnlineWhenHealthSucceeds() {
        Node node = new Node("http://node:8080", 100.0, "key", NodeStatus.OFFLINE, 50.0);
        when(nodeRepository.findAll()).thenReturn(List.of(node));

        RestClient.RequestHeadersUriSpec getSpec = mock();
        RestClient.RequestHeadersSpec headersSpec = mock();
        RestClient.ResponseSpec responseSpec = mock();
        when(restClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.headers(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        scheduler.checkHealth();

        assertEquals(NodeStatus.ONLINE, node.getStatus());
    }

    @Test
    void checkHealth_shouldSetNodeOfflineWhenHealthFails() {
        Node node = new Node("http://node:8080", 100.0, "key", NodeStatus.ONLINE, 50.0);
        when(nodeRepository.findAll()).thenReturn(List.of(node));

        RestClient.RequestHeadersUriSpec getSpec = mock();
        RestClient.RequestHeadersSpec headersSpec = mock();
        RestClient.ResponseSpec responseSpec = mock();
        when(restClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.headers(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenThrow(new RuntimeException("Health check failed: 500"));

        scheduler.checkHealth();

        assertEquals(NodeStatus.OFFLINE, node.getStatus());
    }

    @Test
    void checkHealth_shouldSetNodeOfflineWhenConnectionRefused() {
        Node node = new Node("http://node:8080", 100.0, "key", NodeStatus.ONLINE, 50.0);
        when(nodeRepository.findAll()).thenReturn(List.of(node));

        RestClient.RequestHeadersUriSpec getSpec = mock();
        RestClient.RequestHeadersSpec headersSpec = mock();
        when(restClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.headers(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));

        scheduler.checkHealth();

        assertEquals(NodeStatus.OFFLINE, node.getStatus());
    }

    @Test
    void checkHealth_shouldHandleMultipleNodes() {
        Node node1 = new Node("http://node1:8080", 100.0, "key", NodeStatus.OFFLINE, 50.0);
        Node node2 = new Node("http://node2:8080", 200.0, "key", NodeStatus.ONLINE, 100.0);
        when(nodeRepository.findAll()).thenReturn(List.of(node1, node2));

        RestClient.RequestHeadersUriSpec getSpec = mock();
        RestClient.RequestHeadersSpec headersSpec = mock();
        RestClient.ResponseSpec responseSpec = mock();
        when(restClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.headers(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        scheduler.checkHealth();

        assertEquals(NodeStatus.ONLINE, node1.getStatus());
        assertEquals(NodeStatus.ONLINE, node2.getStatus());
        verify(restClient, times(2)).get();
    }

    @Test
    void checkHealth_shouldDoNothingWhenNoNodes() {
        when(nodeRepository.findAll()).thenReturn(List.of());

        scheduler.checkHealth();

        verifyNoInteractions(restClient);
    }
}
