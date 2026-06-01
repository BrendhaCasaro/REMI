package com.brendhacasaro.remi_node.stored_media.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "stored_media")
public class StoredMedia {
    @Id
    @NonNull
    private UUID id;

    @Column(nullable = false)
    @NonNull
    private String filePath;
}
