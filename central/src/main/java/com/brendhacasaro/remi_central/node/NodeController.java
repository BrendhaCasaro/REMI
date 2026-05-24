package com.brendhacasaro.remi_central.node;

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
    public ResponseEntity<Node> createNode(@RequestBody NodeConfigRequest request) {
        Node node = nodeService.createNode(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(node);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Node> patchNode(@PathVariable Integer id, @RequestBody NodePatchRequest request) {
        Node node = nodeService.patchNode(id, request);

        return ResponseEntity.ok(node);
    }

    @GetMapping
    public ResponseEntity<List<NodeResponse>> getAllNodes() {
        return ResponseEntity.ok(nodeService.getAllNodes());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNode(@PathVariable Integer id) {
        nodeService.deleteNode(id);
        return ResponseEntity.noContent().build();
    }
}
