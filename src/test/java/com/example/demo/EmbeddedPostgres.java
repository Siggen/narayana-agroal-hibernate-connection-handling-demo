package com.example.demo;

import io.zonky.test.db.postgres.embedded.ConnectionInfo;
import io.zonky.test.db.postgres.embedded.LiquibasePreparer;
import io.zonky.test.db.postgres.embedded.PreparedDbProvider;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EmbeddedPostgres {


    @NonNull
    public static PreparedDbProvider createDatabaseProvider() {
        // note: will start a local Postgresql server on first call (one instance per JVM max),
        //       the server stays up until the JVM stops
        var postgresConfig = getPostgresConfig();
        var configCustomizer = (Consumer<io.zonky.test.db.postgres.embedded.EmbeddedPostgres.Builder>) builder -> postgresConfig.forEach(builder::setServerConfig);
        return PreparedDbProvider.forPreparer(LiquibasePreparer.forClasspathLocation("/db/changelog/master.xml"), List.of(configCustomizer));
    }

    public static String getJdbcUri(ConnectionInfo info) {
        var additionalParameters = info.getProperties()
                .entrySet()
                .stream()
                .map((e) -> String.format("&%s=%s", e.getKey(), e.getValue()))
                .collect(Collectors.joining());
        return String.format("jdbc:postgresql://localhost:%d/%s?user=%s", info.getPort(), info.getDbName(), info.getUser()) + additionalParameters;
    }

    protected static Map<String, String> getPostgresConfig() {
        return Map.of(
                "postgres.config.fsync", "off",
                "postgres.config.full_page_writes", "off",
                "postgres.config.autovacuum", "off",
                "postgres.config.max_connections", "40",
                "postgres.config.max_prepared_transactions", "20"
        );
    }
}
