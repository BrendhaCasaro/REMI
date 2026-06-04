package com.brendhacasaro.remi_central.node;

import com.brendhacasaro.remi_central.node.dto.NodeConfigRequest;
import com.brendhacasaro.remi_central.node.dto.NodePatchRequest;
import com.brendhacasaro.remi_central.node.dto.NodeResponse;
import com.brendhacasaro.remi_central.node.model.Node;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nodes")
@RequiredArgsConstructor
public class NodeController {
    private final NodeService nodeService;

    @PostMapping
    public ResponseEntity<NodeResponse> createNode(@RequestBody NodeConfigRequest request) {
        Node node = nodeService.createNode(request);
        NodeResponse response = toResponse(node);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NodeResponse> patchNode(@PathVariable Integer id, @RequestBody NodePatchRequest request) {
        Node node = nodeService.patchNode(id, request);
        NodeResponse response = toResponse(node);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<NodeResponse>> getAllNodes() {
        List<NodeResponse> nodes = nodeService.getAllNodes().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(nodes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNode(@PathVariable Integer id) {
        nodeService.deleteNode(id);
        return ResponseEntity.noContent().build();
    }

    private NodeResponse toResponse(Node node) {
        return new NodeResponse(
                node.getId(),
                node.getUrl(),
                node.getTotalCapacity(),
                node.getStatus()
        );
    }
}
