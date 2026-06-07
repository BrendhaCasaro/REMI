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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NodeMetricsSchedulerTest {

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private RestClient restClient;

    private NodeMetricsScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new NodeMetricsScheduler(nodeRepository, restClient);
    }

    @Test
    void updateMetrics_shouldUpdateDiskFree() {
        Node node = new Node("http://node:8080", 100.0, "key", NodeStatus.ONLINE, 50.0);
        when(nodeRepository.findAll()).thenReturn(List.of(node));

        RestClient.RequestHeadersUriSpec getSpec = mock();
        RestClient.RequestHeadersSpec headersSpec = mock();
        RestClient.ResponseSpec responseSpec = mock();
        when(restClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.headers(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(eq(com.brendhacasaro.remi_central.orchestrator.dto.MetricsResponse.class)))
                .thenReturn(new com.brendhacasaro.remi_central.orchestrator.dto.MetricsResponse(42.5));

        scheduler.updateMetrics();

        assertEquals(42.5, node.getDiskFree(), 0.001);
    }

    @Test
    void updateMetrics_shouldHandleMultipleNodes() {
        Node node1 = new Node("http://node1:8080", 100.0, "key", NodeStatus.ONLINE, 50.0);
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
        when(responseSpec.body(any(Class.class))).thenReturn(
                new com.brendhacasaro.remi_central.orchestrator.dto.MetricsResponse(10.0));

        scheduler.updateMetrics();

        assertEquals(10.0, node1.getDiskFree(), 0.001);
        assertEquals(10.0, node2.getDiskFree(), 0.001);
        verify(restClient, times(2)).get();
    }

    @Test
    void updateMetrics_shouldKeepOldValueWhenRequestFails() {
        Node node = new Node("http://node:8080", 100.0, "key", NodeStatus.ONLINE, 50.0);
        when(nodeRepository.findAll()).thenReturn(List.of(node));

        RestClient.RequestHeadersUriSpec getSpec = mock();
        RestClient.RequestHeadersSpec headersSpec = mock();
        RestClient.ResponseSpec responseSpec = mock();
        when(restClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.headers(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenThrow(new RuntimeException("Metrics request failed: 500"));

        scheduler.updateMetrics();

        assertEquals(50.0, node.getDiskFree(), 0.001);
    }

    @Test
    void updateMetrics_shouldKeepOldValueWhenConnectionRefused() {
        Node node = new Node("http://node:8080", 100.0, "key", NodeStatus.ONLINE, 50.0);
        when(nodeRepository.findAll()).thenReturn(List.of(node));

        RestClient.RequestHeadersUriSpec getSpec = mock();
        RestClient.RequestHeadersSpec headersSpec = mock();
        when(restClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.headers(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));

        scheduler.updateMetrics();

        assertEquals(50.0, node.getDiskFree(), 0.001);
    }

    @Test
    void updateMetrics_shouldDoNothingWhenNoNodes() {
        when(nodeRepository.findAll()).thenReturn(List.of());

        scheduler.updateMetrics();

        verifyNoInteractions(restClient);
    }
}
