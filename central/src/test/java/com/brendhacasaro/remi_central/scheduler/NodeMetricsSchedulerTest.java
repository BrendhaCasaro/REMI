package com.brendhacasaro.remi_central.scheduler;

import com.brendhacasaro.remi_central.node.NodeMetricsClient;
import com.brendhacasaro.remi_central.node.NodeRepository;
import com.brendhacasaro.remi_central.node.NodeStatus;
import com.brendhacasaro.remi_central.node.model.Node;
import com.brendhacasaro.remi_central.orchestrator.dto.MetricsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NodeMetricsSchedulerTest {

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private NodeMetricsClient metricsClient;

    private NodeMetricsScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new NodeMetricsScheduler(nodeRepository, metricsClient);
    }

    @Test
    void updateMetrics_shouldUpdateDiskFree() {
        Node node = new Node("http://node:8080", 100.0, "key", NodeStatus.ONLINE, 50.0);
        when(nodeRepository.findAll()).thenReturn(List.of(node));
        when(metricsClient.fetchMetrics(node)).thenReturn(new MetricsResponse(42.5));

        scheduler.updateMetrics();

        assertEquals(42.5, node.getDiskFree(), 0.001);
    }

    @Test
    void updateMetrics_shouldHandleMultipleNodes() {
        Node node1 = new Node("http://node1:8080", 100.0, "key", NodeStatus.ONLINE, 50.0);
        Node node2 = new Node("http://node2:8080", 200.0, "key", NodeStatus.ONLINE, 100.0);
        when(nodeRepository.findAll()).thenReturn(List.of(node1, node2));
        when(metricsClient.fetchMetrics(any())).thenReturn(new MetricsResponse(10.0));

        scheduler.updateMetrics();

        assertEquals(10.0, node1.getDiskFree(), 0.001);
        assertEquals(10.0, node2.getDiskFree(), 0.001);
        verify(metricsClient, times(2)).fetchMetrics(any());
    }

    @Test
    void updateMetrics_shouldKeepOldValueWhenRequestFails() {
        Node node = new Node("http://node:8080", 100.0, "key", NodeStatus.ONLINE, 50.0);
        when(nodeRepository.findAll()).thenReturn(List.of(node));
        when(metricsClient.fetchMetrics(node)).thenThrow(new RuntimeException("Metrics request failed: 500"));

        scheduler.updateMetrics();

        assertEquals(50.0, node.getDiskFree(), 0.001);
    }

    @Test
    void updateMetrics_shouldKeepOldValueWhenConnectionRefused() {
        Node node = new Node("http://node:8080", 100.0, "key", NodeStatus.ONLINE, 50.0);
        when(nodeRepository.findAll()).thenReturn(List.of(node));
        when(metricsClient.fetchMetrics(node)).thenThrow(new RuntimeException("Connection refused"));

        scheduler.updateMetrics();

        assertEquals(50.0, node.getDiskFree(), 0.001);
    }

    @Test
    void updateMetrics_shouldDoNothingWhenNoNodes() {
        when(nodeRepository.findAll()).thenReturn(List.of());

        scheduler.updateMetrics();

        verifyNoInteractions(metricsClient);
    }
}
