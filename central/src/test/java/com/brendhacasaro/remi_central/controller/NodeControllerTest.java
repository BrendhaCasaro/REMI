package com.brendhacasaro.remi_central.controller;

import com.brendhacasaro.remi_central.auth.JwtAuthenticationFilter;
import com.brendhacasaro.remi_central.auth.SecurityConfig;
import com.brendhacasaro.remi_central.config.TestSecurityConfig;
import com.brendhacasaro.remi_central.node.NodeController;
import com.brendhacasaro.remi_central.node.NodeService;
import com.brendhacasaro.remi_central.node.NodeStatus;
import com.brendhacasaro.remi_central.node.dto.NodeConfigRequest;
import com.brendhacasaro.remi_central.node.dto.NodePatchRequest;
import com.brendhacasaro.remi_central.node.model.Node;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NodeController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthenticationFilter.class}))
@Import(TestSecurityConfig.class)
class NodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private NodeService nodeService;

    private final UUID nodeKey = UUID.randomUUID();

    @Test
    void createNode_shouldReturn201() throws Exception {
        when(nodeService.createNode(any())).thenReturn(
                new Node("http://node:8080", 100.0, nodeKey, NodeStatus.ONLINE));

        mockMvc.perform(post("/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new NodeConfigRequest("http://node:8080", 100.0, nodeKey, NodeStatus.ONLINE))))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllNodes_shouldReturn200() throws Exception {
        when(nodeService.getAllNodes()).thenReturn(List.of(
                new Node("http://a:8080", 100.0, nodeKey, NodeStatus.ONLINE),
                new Node("http://b:8080", 200.0, nodeKey, NodeStatus.OFFLINE)));

        mockMvc.perform(get("/nodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].url").value("http://a:8080"));
    }

    @Test
    void patchNode_shouldReturn200() throws Exception {
        when(nodeService.patchNode(any(), any())).thenReturn(
                new Node("http://patched:8080", 150.0, nodeKey, NodeStatus.ONLINE));

        mockMvc.perform(patch("/nodes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new NodePatchRequest("http://patched:8080", 150.0, nodeKey, NodeStatus.ONLINE))))
                .andExpect(status().isOk());
    }

    @Test
    void deleteNode_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/nodes/1"))
                .andExpect(status().isNoContent());
    }
}
