package com.brendhacasaro.remi_central;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.brendhacasaro.remi_central.config.TestcontainersConfig;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfig.class)
class DigitalMediaApplicationTests {

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
