package com.brendhacasaro.remi_central.node_media;

import com.brendhacasaro.remi_central.media.model.Media;
import com.brendhacasaro.remi_central.node.model.Node;
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
@Table(name = "node_media")
@Entity
@DynamicUpdate
public class NodeMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "node_id")
    private Node node;

    @ManyToOne
    @JoinColumn(name = "media_id")
    private Media media;
}
