package com.brendhacasaro.digital_media.node;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "node")
@Entity
@DynamicUpdate
public class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String url;

    @Column
    private double totalCapacity;

    @Column
    private UUID key;

    @Column
    @Enumerated(EnumType.STRING)
    private NodeStatus status;
}
