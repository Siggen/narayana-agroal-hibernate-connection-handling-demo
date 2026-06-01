package com.example.demo;

import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(EmbeddedPostgres.class)
@SpringBootTest
class DemoApplicationTests {

    @TestConfiguration
    static class Configuration {

        @Bean
        public SpringLiquibase liquibase() {
            var liquibase = new SpringLiquibase();
            liquibase.setShouldRun(false);    // already ran by EmbeddedPostgres.createDatabaseProvider()
            return liquibase;
        }
    }

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) throws SQLException {

        // create embedded database
        var provider = EmbeddedPostgres.createDatabaseProvider();
        var connectionInfo = provider.createNewDatabase();
        var url = EmbeddedPostgres.getJdbcUri(connectionInfo);

        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", connectionInfo::getUser);
        registry.add("spring.datasource.password", () -> "");
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
