package com.brendhacasaro.remi_node.stored_media.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity
@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "stored_media")
public class StoredMedia {
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @NonNull
    private UUID id;

    @Column(nullable = false)
    @NonNull
    private String filePath;
}
