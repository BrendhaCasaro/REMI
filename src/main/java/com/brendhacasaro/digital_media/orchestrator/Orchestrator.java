package com.brendhacasaro.digital_media.orchestrator;

import com.brendhacasaro.digital_media.node.Node;
import com.brendhacasaro.digital_media.node.NodeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class Orchestrator {
    private final NodeRepository nodeRepository;
    private final RestClient restClient = RestClient.create();

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
                boolean healthy = restClient.get()
                        .uri(node.getUrl() + "/healthchecker")
                        .exchange((request, response) ->
                                response.getStatusCode().is2xxSuccessful()
                        );

                if (healthy) {
                    nodesOk.add(node);
                }

            } catch (Exception _) {
            }
        }

        return nodesOk;
    }

    private Node storageChecker(List<Node> nodesOk) {
        // verifica e retorna o melhor node (dentre os funcionais)
        // que tem o maior armazenamento e o retorna

        for (Node node : nodesOk) {

        }
    }
}
