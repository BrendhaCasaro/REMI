package com.brendhacasaro.remi_central.media.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
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

    @CreationTimestamp
    private LocalDateTime createdAt;
}
