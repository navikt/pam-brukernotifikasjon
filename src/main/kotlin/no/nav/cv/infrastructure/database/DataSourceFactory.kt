package no.nav.cv.infrastructure.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@ConditionalOnProperty(value = ["NAIS_CLUSTER_NAME"])
class VaultDatasourceFactory(
        @Value("\${db.vault.path}") private val vaultPath: String,
        @Value("\${db.vault.username}") private val username: String,
        @Value("\${datasources.default.jdbcUrl}") private val jdbcUrl: String,
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(VaultDatasourceFactory::class.java)
    }

    @Bean
    fun flywayConfig(
            dataSourceProperties: DataSourceProperties,
    ): FlywayConfigurationCustomizer {
        return FlywayConfigurationCustomizer {
            it.initSql("SET ROLE pam-brukernotifikasjon-admin")
                    .dataSource(HikariCPVaultUtil
                            .createHikariDataSourceWithVaultIntegration(hikariConfig(dataSourceProperties), vaultPath, username))

        }
    }

    @Bean
    fun navHikariDatasource(
            dataSourceProperties: DataSourceProperties,
    ): HikariDataSource {
        return HikariCPVaultUtil
                .createHikariDataSourceWithVaultIntegration(hikariConfig(dataSourceProperties), vaultPath, username)

    }

    private fun hikariConfig(dataSourceProperties: DataSourceProperties): HikariConfig {
        val config = HikariConfig()
        config.minimumIdle = 0
        config.maximumPoolSize = 4
        config.idleTimeout = 10001
        config.maxLifetime = 30001
        config.connectionTestQuery = "select 1"
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = jdbcUrl
        return config
    }

}
