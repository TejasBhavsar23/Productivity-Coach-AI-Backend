package com.productivitycoach;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test — verifies the Spring application context loads correctly.
 * Uses H2 in-memory DB so it runs without a real PostgreSQL instance.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.jwt.secret=TestSecretKeyThatIsLongEnoughForHS256AlgorithmToWork1234",
        "app.jwt.expiration-ms=86400000",
        "ai.claude.api-key=test-key",
        "ai.claude.base-url=https://api.anthropic.com",
        "ai.claude.model=claude-3-5-sonnet-20241022",
        "ai.claude.max-tokens=100",
        "ai.claude.api-version=2023-06-01"
})
class AiProductivityCoachApplicationTests {

    @Test
    void contextLoads() {
        // If the Spring context starts without throwing, this test passes.
    }
}
