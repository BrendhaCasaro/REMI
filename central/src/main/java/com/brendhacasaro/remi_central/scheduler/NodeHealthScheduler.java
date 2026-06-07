package com.brendhacasaro.remi_central.scheduler;

import com.brendhacasaro.remi_central.node.NodeRepository;
import com.brendhacasaro.remi_central.node.NodeStatus;
import com.brendhacasaro.remi_central.node.model.Node;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
public class NodeHealthScheduler {
    private final NodeRepository nodeRepository;
    private final RestClient restClient;

    public NodeHealthScheduler(NodeRepository nodeRepository, RestClient.Builder restClientBuilder) {
        this.nodeRepository = nodeRepository;
        this.restClient = restClientBuilder.build();
    }

    // Apenas para injeção de mock RestClient nos testes
    NodeHealthScheduler(NodeRepository nodeRepository, RestClient restClient) {
        this.nodeRepository = nodeRepository;
        this.restClient = restClient;
    }

    private static final long ONE_MINUTE_MS = 60_000;

    @Transactional
    @Scheduled(fixedRate = ONE_MINUTE_MS)
    public void checkHealth() {
        List<Node> nodes = nodeRepository.findAll();
        for (Node node : nodes) {
            try {
                restClient.get()
                        .uri(node.getUrl() + "/api/health")
                        .headers(headers -> headers.setBearerAuth(node.getKey()))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, (req, res) -> {
                            throw new RuntimeException("Health check failed: " + res.getStatusCode());
                        })
                        .toBodilessEntity();
                node.setStatus(NodeStatus.ONLINE);
                log.debug("Node {} is ONLINE", node.getUrl());
            } catch (Exception e) {
                node.setStatus(NodeStatus.OFFLINE);
                log.warn("Node {} is OFFLINE: {}", node.getUrl(), e.getMessage());
            }
        }
    }
}
