package no.nav.cv.infrastructure.database

import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration
class ShedlockConfig(
        private val datasource: DataSource
) {

    @Bean
    fun lockProvider() : JdbcTemplateLockProvider {
        return JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(JdbcTemplate(datasource))
                        .usingDbTime() // Works on Postgres, MySQL, MariaDb, MS SQL, Oracle, DB2, HSQL and H2
                        .build()
        );
    }

}
