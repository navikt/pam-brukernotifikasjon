package no.nav.cv.infrastructure.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.micronaut.configuration.hibernate.jpa.EntityManagerFactoryBean
import io.micronaut.context.annotation.*
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.context.event.BeanInitializedEventListener
import io.micronaut.context.event.BeanInitializingEvent
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.hibernate.boot.MetadataSources
import javax.inject.Singleton
import javax.sql.DataSource


@Factory
@Requires(notEnv = [ "test" ])
class VaultDataSource(
        @Value("\${db.vault.path}") private val vaultPath: String,
        @Value("\${db.vault.username}") private val username: String,
        @Value("\${db.vault.connection.string}") private val url: String
) : BeanInitializedEventListener<DataSource> {

    @Singleton
    @Primary
    fun vaultDataSource(): HikariDataSource {

        val config = HikariConfig()

        with(config) {
            minimumIdle = 0
            maximumPoolSize = 4
            idleTimeout = 10001
            maxLifetime = 30001
            connectionTestQuery = "select 1"
            jdbcUrl = url

        }

        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, vaultPath, username)
    }

    override fun onInitialized(event: BeanInitializingEvent<DataSource>?) = vaultDataSource()


}