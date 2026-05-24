package com.brendhacasaro.remi.node;

import com.brendhacasaro.remi.media.MediaRepository;
import com.brendhacasaro.remi.node_media.NodeMediaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NodeService {
    private final NodeRepository nodeRepository;
    private final NodeMediaRepository nodeMediaRepository;
    private final MediaRepository mediaRepository;

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

    @Transactional
    public Node patchNode(Integer id, NodePatchRequest request) {
        Node node = nodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Node id " + id + " not found"));

        if (request.url() != null && !request.url().equals(node.getUrl())) {
            node.setUrl(request.url());
        }

        if (request.totalCapacity() != null && !request.totalCapacity().equals(node.getTotalCapacity())) {
            node.setTotalCapacity(request.totalCapacity());
        }

        if (request.key() != null && !request.key().equals(node.getKey())) {
            node.setKey(request.key());
        }

        if (request.status() != null && request.status() != node.getStatus()) {
            node.setStatus(request.status());
        }

        return nodeRepository.save(node);
    }

    public List<NodeResponse> getAllNodes() {
        return nodeRepository.findAll()
                .stream()
                .map(node -> new NodeResponse(
                        node.getUrl(),
                        node.getTotalCapacity(),
                        node.getStatus()
                ))
                .toList();
    }

    @Transactional
    public void deleteNode(Integer id) {
        Node node = nodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Node id " + id + " not found"));

        List<UUID> mediaIds = nodeMediaRepository.findMediaIdsByNodeId(id);
        nodeMediaRepository.deleteByNodeId(id);

        if (!mediaIds.isEmpty()) {
            mediaRepository.deleteAllById(mediaIds);
        }

        nodeRepository.delete(node);
    }
}
