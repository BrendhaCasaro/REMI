package com.brendhacasaro.remi_node.media;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "node_media_file")
public class NodeMediaFile {
    @Id
    private UUID mediaId;

    @Column(nullable = false)
    private String filePath;

    public NodeMediaFile() {
    }

    public NodeMediaFile(UUID mediaId, String filePath) {
        this.mediaId = mediaId;
        this.filePath = filePath;
    }

    public UUID getMediaId() {
        return mediaId;
    }

    public void setMediaId(UUID mediaId) {
        this.mediaId = mediaId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
