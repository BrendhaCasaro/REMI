package com.brendhacasaro.remi_central.scheduler;

import com.brendhacasaro.remi_central.node.NodeRepository;
import com.brendhacasaro.remi_central.node.NodeStatus;
import com.brendhacasaro.remi_central.node.model.Node;
import com.brendhacasaro.remi_central.orchestrator.dto.MetricsResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
public class NodeMetricsScheduler {
    private final NodeRepository nodeRepository;
    private final RestClient restClient;

    public NodeMetricsScheduler(NodeRepository nodeRepository, RestClient.Builder restClientBuilder) {
        this.nodeRepository = nodeRepository;
        this.restClient = restClientBuilder.build();
    }

    // Apenas para injeção de mock RestClient nos testes
    NodeMetricsScheduler(NodeRepository nodeRepository, RestClient restClient) {
        this.nodeRepository = nodeRepository;
        this.restClient = restClient;
    }

    private static final long FIFTEEN_MINUTES_MS = 15 * 60_000;

    @Transactional
    @Scheduled(fixedRate = FIFTEEN_MINUTES_MS)
    public void updateMetrics() {
        List<Node> nodes = nodeRepository.findAll();
        for (Node node : nodes) {
            try {
                MetricsResponse metrics = restClient.get()
                        .uri(node.getUrl() + "/api/metrics")
                        .headers(headers -> headers.setBearerAuth(node.getKey()))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, (req, res) -> {
                            throw new RuntimeException("Metrics request failed: " + res.getStatusCode());
                        })
                        .body(MetricsResponse.class);
                node.setDiskFree(metrics.diskFree());
                log.debug("Updated disk free for node {}: {} GB", node.getUrl(), metrics.diskFree());
            } catch (Exception e) {
                log.warn("Failed to get metrics for node {}: {}", node.getUrl(), e.getMessage());
            }
        }
    }
}
