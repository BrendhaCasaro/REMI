package com.brendhacasaro.digital_media.node;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "storage_node")
@Entity
@DynamicUpdate
public class Node {
    @Id
    private int id;

    @Column
    private String url;

    @Column
    private double totalCapacity;

    @Column
    private double usedCapacity;

    @Column
    @Enumerated(EnumType.STRING)
    private NodeStatus status;
}
