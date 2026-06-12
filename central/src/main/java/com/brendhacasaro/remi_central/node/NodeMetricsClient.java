package com.brendhacasaro.remi_central.node;

import com.brendhacasaro.remi_central.node.model.Node;
import com.brendhacasaro.remi_central.orchestrator.dto.MetricsResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NodeMetricsClient {
    private final RestClient restClient;

    public NodeMetricsClient() {
        this.restClient = RestClient.create();
    }

    NodeMetricsClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public MetricsResponse fetchMetrics(Node node) {
        return restClient.get()
                .uri(node.getUrl() + "/api/metrics")
                .headers(headers -> headers.setBearerAuth(node.getKey()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new RuntimeException("Metrics request failed: " + res.getStatusCode());
                })
                .body(MetricsResponse.class);
    }
}
