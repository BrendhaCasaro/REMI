package com.brendhacasaro.digital_media.node;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NodeService {
    private final NodeRepository nodeRepository;

    @Transactional
    public Node createNode(NodeConfigRequest request) {
        Node node = new Node(
                request.url(),
                request.totalCapacity(),
                request.key(),
                request.status()
        );

        return nodeRepository.save(node);
    }
}
