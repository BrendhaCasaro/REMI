package com.brendhacasaro.digital_media.node;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class NodeController {
    private final NodeService nodeService;

    @PostMapping
    public ResponseEntity<Node> createNode(@RequestBody NodeConfigRequest request) {
        Node node = nodeService.createNode(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(node);
    }
}
