package com.brendhacasaro.remi_central.media;

import com.brendhacasaro.remi_central.config.TestcontainersConfig;
import com.brendhacasaro.remi_central.media.model.Media;
import com.brendhacasaro.remi_central.node.NodeRepository;
import com.brendhacasaro.remi_central.node.NodeStatus;
import com.brendhacasaro.remi_central.node.model.Node;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfig.class)
class MediaRepositoryTest {

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Test
    void saveAndFindById() {
        Media media = new Media("photo.jpg");
        media = mediaRepository.save(media);

        var found = mediaRepository.findById(media.getId());

        assertTrue(found.isPresent());
        assertEquals("photo.jpg", found.get().getName());
    }

    @Test
    void findByNodeId_shouldReturnAssociatedMedia() {
        Node node = nodeRepository.save(
                new Node("http://node:8080", 100.0, UUID.randomUUID(), NodeStatus.ONLINE));
        Media media1 = new Media("file1.txt");
        media1.setNode(node);
        Media media2 = new Media("file2.txt");
        media2.setNode(node);
        mediaRepository.save(media1);
        mediaRepository.save(media2);

        List<Media> medias = mediaRepository.findByNodeId(node.getId());

        assertEquals(2, medias.size());
        assertTrue(medias.stream().allMatch(m -> m.getNode().getId().equals(node.getId())));
    }

    @Test
    void findByNodeId_shouldReturnEmptyWhenNoMedia() {
        List<Media> medias = mediaRepository.findByNodeId(999);

        assertTrue(medias.isEmpty());
    }
}
