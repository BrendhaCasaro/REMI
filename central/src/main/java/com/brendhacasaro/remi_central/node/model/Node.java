package com.brendhacasaro.remi_central.node.model;

import com.brendhacasaro.remi_central.node.NodeStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.util.UUID;

@Table(name = "node")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@DynamicUpdate
public class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    @NonNull
    private String url;

    @Column
    @NonNull
    private Double totalCapacity;

    @Column
    @NonNull
    private UUID key;

    @Column
    @NonNull
    @Enumerated(EnumType.STRING)
    private NodeStatus status;
}
