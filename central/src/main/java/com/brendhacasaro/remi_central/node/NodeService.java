package com.brendhacasaro.remi_central.node;

import com.brendhacasaro.remi_central.media.MediaRepository;
import com.brendhacasaro.remi_central.media.model.Media;
import com.brendhacasaro.remi_central.node.dto.NodeConfigRequest;
import com.brendhacasaro.remi_central.node.dto.NodePatchRequest;
import com.brendhacasaro.remi_central.node.dto.NodeResponse;
import com.brendhacasaro.remi_central.node.model.Node;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NodeService {
    private final NodeRepository nodeRepository;
    private final MediaRepository mediaRepository;

    @Transactional
    public NodeResponse createNode(NodeConfigRequest request) {
        Node node = new Node(
                request.url(),
                request.totalCapacity(),
                request.key(),
                request.status()
        );

        node = nodeRepository.save(node);
        return toResponse(node);
    }

    @Transactional
    public NodeResponse patchNode(Integer id, NodePatchRequest request) {
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

        node = nodeRepository.save(node);
        return toResponse(node);
    }

    public List<NodeResponse> getAllNodes() {
        return nodeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private NodeResponse toResponse(Node node) {
        return new NodeResponse(
                node.getId(),
                node.getUrl(),
                node.getTotalCapacity(),
                node.getStatus()
        );
    }

    @Transactional
    public void deleteNode(Integer id) {
        Node node = nodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Node id " + id + " not found"));

        List<Media> medias = mediaRepository.findByNodeId(id);

        if (!medias.isEmpty()) {
            mediaRepository.deleteAll(medias);
        }

        nodeRepository.delete(node);
    }
}
