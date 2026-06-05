package com.brendhacasaro.remi_central.orchestrator;

import com.brendhacasaro.remi_central.node.NodeRepository;
import com.brendhacasaro.remi_central.node.NodeStatus;
import com.brendhacasaro.remi_central.node.model.Node;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class Orchestrator {
    private final NodeRepository nodeRepository;

    public Node chooseNode() {
        return nodeRepository.findTopByStatusOrderByDiskFreeDesc(NodeStatus.ONLINE)
                .orElseThrow(() -> new OrchestratorException("There is no Nodes available"));
    }
}
