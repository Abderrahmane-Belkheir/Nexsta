package com.Nexsta.IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestConfiguration
public abstract class TestContainerConfig {
@Autowired
private JdbcTemplate jdbcTemplate;
@Autowired
private RedisTemplate<String,String> redisTemplate;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine").withExposedPorts(5432).withUsername("test").withPassword("test");


    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7.2-alpine")
                    .withExposedPorts(6379);


    @Container
    static GenericContainer<?> keycloak=new GenericContainer<>("quay.io/keycloak/keycloak:21.1.1")
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_ADMIN", "admin")
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
            .withCommand("start-dev");
static {
    postgres.start();
    redis.start();
    keycloak.start();
}
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                ()->"http://localhost:8080/realms/Realm");
        registry.add("keycloak.username",()->"admin");
        registry.add("keycloak.password",()->"Admin123!");
    }


   // @BeforeEach
    public void cleanup(){
   jdbcTemplate.execute("TRUNCATE TABLE USERS CASCADE");
   jdbcTemplate.execute("TRUNCATE TABLE FOLLOW CASCADE");
   jdbcTemplate.execute("TRUNCATE TABLE BLOCK CASCADE");
redisTemplate.getConnectionFactory().getConnection().flushAll();
}
}
