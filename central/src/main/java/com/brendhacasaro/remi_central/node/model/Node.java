package com.brendhacasaro.remi_central.node.model;

import com.brendhacasaro.remi_central.node.NodeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Table(name = "nodes")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
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
    private String key;

    @Column
    @NonNull
    @Enumerated(EnumType.STRING)
    private NodeStatus status;

    @Column
    @NonNull
    private Double diskFree;
}
