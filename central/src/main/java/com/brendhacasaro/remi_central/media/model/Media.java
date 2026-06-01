package com.brendhacasaro.remi_central.media.model;

import com.brendhacasaro.remi_central.node.model.Node;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "medias")
@Entity
public class Media {
    @Id
    @UuidGenerator
    private UUID id;

    @Column
    @NonNull
    private String name;

    @ManyToOne
    @JoinColumn(name = "node_id")
    private Node node;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
