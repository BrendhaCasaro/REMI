package com.brendhacasaro.remi_node;

import com.brendhacasaro.remi_node.config.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestcontainersConfig.class)
class NodeDigitalStoredMediaApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
    }

    @Test
    void databaseConnectionShouldWork() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5));
        }
    }

}
