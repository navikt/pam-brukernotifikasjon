package no.nav.cv.infrastructure.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import javax.inject.Singleton


@Factory
@Requires(notEnv = [ "test" ])
class VaultDataSource(
        @Value("\${db.vault.path}") private val vaultPath: String,
        @Value("\${db.vault.username}") private val username: String,
        @Value("\${db.vault.connection.string}") private val url: String
) {

    @Singleton
    fun vaultDataSource(
    ): HikariDataSource {

        val config = HikariConfig()

        with(config) {
            minimumIdle = 0
            maximumPoolSize = 4
            idleTimeout = 10001
            maxLifetime = 30001
            connectionTestQuery = "select 1"
            jdbcUrl = url

            connectionInitSql = "SET ROLE \"$username\""
        }

        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, vaultPath, username)
    }

}