package no.nav.cv

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest
@ActiveProfiles("test")
abstract class SingletonPostgresTestInstance {

    companion object {
        val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15-alpine").apply {
            start()
            System.setProperty("spring.datasource.url", jdbcUrl)
            System.setProperty("spring.datasource.username", username)
            System.setProperty("spring.datasource.password", password)
        }
    }

    @Autowired
    lateinit var flyway : Flyway

    @BeforeEach
    fun prepareSchema() {
        ventTilDatabasenHarStartet()
        flyway.migrate()
    }

    private fun ventTilDatabasenHarStartet() {
        while (!postgresContainer.isRunning()) {
            println("Test-container-en for Postgres er ikke klar enda, venter til den er klar før vi går videre")
            Thread.sleep(500)
        }
    }

    @AfterEach
    fun clearTestdata() {
        flyway.clean()
    }

}
