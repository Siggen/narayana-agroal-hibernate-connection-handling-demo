package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class DemoApplicationTests {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer(System.getProperty("postgres.testcontainer.image", "postgres:17-alpine"));

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());
    }

    /**
     * Test hibernate connection handling mode with agroal connection poool
     * (see <i>spring.jpa.properties.hibernate.connection.handling_mode</i> property in <i>application.properties</i> file for
     * available modes)
     */
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void connectionHandling(CapturedOutput output) {

        // execute some transaction
        var template = new TransactionTemplate(transactionManager);
        var users = template.execute(_ -> userRepository.findAll());
        assertEquals(0, users.size());

        // expect no warning
        assertEquals("", output.getOut());
    }
}
