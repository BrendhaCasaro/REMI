package com.brendhacasaro.digital_media.media.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "media")
@Entity
@DynamicUpdate
public class Media {
    @Id
    @UuidGenerator
    private UUID id;

    @Column
    private String name;

    @Column
    private String urlNode;

    @Column
    @Enumerated(EnumType.STRING)
    private MediaStatus status = MediaStatus.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
