package com.brendhacasaro.remi_central.orchestrator;

import com.brendhacasaro.remi_central.node.NodeRepository;
import com.brendhacasaro.remi_central.node.model.Node;
import com.brendhacasaro.remi_central.orchestrator.dto.MetricsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

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
        // ignorar nodes com health check em erro
        // selecionar node com maior espaço livre
        // em caso de empate, manter o primeiro encontrado
        // caso nenhum node esteja disponível, lançar exceção

        return storageChecker(nodeHealthChecker());
    }

    private List<Node> nodeHealthChecker() {
        // para cada node da lista, enviar a requisição para o endpoint do node
        // retorna lista com os nodes com status code !erro

        List<Node> nodes = new ArrayList<>(nodeRepository.findAll());
        List<Node> nodesOk = new ArrayList<>();

        for (Node node : nodes) {
            try {
                boolean healthy = Boolean.TRUE.equals(restClient.get()
                        .uri(node.getUrl() + "/api/health")
                        .exchange((request, response) ->
                                response.getStatusCode().is2xxSuccessful()
                        ));

                if (healthy) {
                    nodesOk.add(node);
                }

            } catch (Exception e) {
            }
        }

        return nodesOk;
    }

    private Node storageChecker(List<Node> nodesOk) {
        // verifica e retorna o melhor node (dentre os funcionais)
        // que tem o maior espaço livre e o retorna

        if (nodesOk.isEmpty()) {
            throw new OrchestratorException("There is no Nodes available");
        }

        Node betterNode = nodesOk.getFirst();
        Double betterDisk = -1.0;

        for (Node node : nodesOk) {
            try {
                MetricsResponse metricsResponse = restClient.get()
                        .uri(node.getUrl() + "/api/metrics")
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, (req, res) -> {
                            throw new RestClientException("HTTP error: " + res.getStatusCode() + res.getBody());
                        })
                        .body(MetricsResponse.class);

                if (metricsResponse.diskFree() > betterDisk || betterDisk == -1.0) {
                    betterDisk = metricsResponse.diskFree();
                    betterNode = node;
                }
            } catch (Exception e) {
                throw new OrchestratorException("Error to connect to nodes", e);
            }
        }
        return betterNode;
    }
}
