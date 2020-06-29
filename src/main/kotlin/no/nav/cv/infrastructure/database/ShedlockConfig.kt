package no.nav.cv.infrastructure.database

import io.micronaut.context.annotation.Factory
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import org.springframework.jdbc.core.JdbcTemplate
import javax.inject.Singleton
import javax.sql.DataSource

@Factory
class ShedlockConfig(
        private val datasource: DataSource
) {

    @Singleton
    fun lockProvider() : JdbcTemplateLockProvider {
        return JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(JdbcTemplate(datasource))
                        .usingDbTime() // Works on Postgres, MySQL, MariaDb, MS SQL, Oracle, DB2, HSQL and H2
                        .build()
                );
    }

}