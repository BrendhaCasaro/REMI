package com.brendhacasaro.remi.media.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "media")
@Entity
@DynamicUpdate
public class Media {
    @Id
    @UuidGenerator
    private UUID id;

    @Column
    @NonNull
    private String name;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
