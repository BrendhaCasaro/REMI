package com.brendhacasaro.remi_central.scheduler;

import com.brendhacasaro.remi_central.node.NodeMetricsClient;
import com.brendhacasaro.remi_central.node.NodeRepository;
import com.brendhacasaro.remi_central.node.model.Node;
import com.brendhacasaro.remi_central.orchestrator.dto.MetricsResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NodeMetricsScheduler {
    private final NodeRepository nodeRepository;
    private final NodeMetricsClient metricsClient;

    public NodeMetricsScheduler(NodeRepository nodeRepository, NodeMetricsClient metricsClient) {
        this.nodeRepository = nodeRepository;
        this.metricsClient = metricsClient;
    }

    private static final long FIFTEEN_MINUTES_MS = 15 * 60_000;

    @Transactional
    @Scheduled(fixedRate = FIFTEEN_MINUTES_MS)
    public void updateMetrics() {
        List<Node> nodes = nodeRepository.findAll();
        for (Node node : nodes) {
            try {
                MetricsResponse metrics = metricsClient.fetchMetrics(node);
                node.setDiskFree(metrics.diskFree());
                log.debug("Updated disk free for node {}: {} GB", node.getUrl(), metrics.diskFree());
            } catch (Exception e) {
                log.warn("Failed to get metrics for node {}: {}", node.getUrl(), e.getMessage());
            }
        }
    }
}
