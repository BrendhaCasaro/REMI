package com.brendhacasaro.remi_central.orchestrator;

import com.brendhacasaro.remi_central.node.NodeRepository;
import com.brendhacasaro.remi_central.node.NodeStatus;
import com.brendhacasaro.remi_central.node.model.Node;
import com.brendhacasaro.remi_central.orchestrator.dto.MetricsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
public class Orchestrator {
    private final NodeRepository nodeRepository;
    private final RestClient restClient;

    @Autowired
    public Orchestrator(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
        this.restClient = RestClient.create();
    }

    public Orchestrator(NodeRepository nodeRepository, RestClient restClient) {
        this.nodeRepository = nodeRepository;
        this.restClient = restClient;
    }

    public Node chooseNode() {
        // carregar nodes ativos
        // verificar saúde e métricas em uma única chamada
        // selecionar node com maior espaço livre
        // em caso de empate, manter o primeiro encontrado
        // caso nenhum node esteja disponível, lançar exceção

        return storageChecker();
    }

    private Node storageChecker() {
        List<Node> nodes = nodeRepository.findByStatus(NodeStatus.ONLINE);

        Node betterNode = null;
        Double betterDisk = null;

        for (Node node : nodes) {
            try {
                MetricsResponse metricsResponse = restClient.get()
                        .uri(node.getUrl() + "/api/metrics")
                        .retrieve()
                        .body(MetricsResponse.class);

                if (betterDisk == null || metricsResponse.diskFree() > betterDisk) {
                    betterDisk = metricsResponse.diskFree();
                    betterNode = node;
                }
            } catch (Exception e) {
                log.warn("Failed to check metrics for node {}: {}", node.getUrl(), e.getMessage());
            }
        }

        if (betterNode == null) {
            throw new OrchestratorException("There is no Nodes available");
        }

        return betterNode;
    }
}
