package no.nav.cv.event.oppfolgingstatus

import io.micronaut.spring.tx.annotation.Transactional
import java.math.BigInteger
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Singleton
open class FeedMetadataRepository(
        @PersistenceContext private val entityManager: EntityManager
) {

    @Transactional
    open fun sisteFeedId(): Long {
        return (entityManager.createNativeQuery(
                "SELECT INDEX FROM FEED_METADATA WHERE ID = 'OPPFOLGING_FEED';"
        ).singleResult as BigInteger).longValueExact()
    }

    @Transactional
    open fun oppdaterFeedId(value: Long) {
        entityManager.createNativeQuery("UPDATE FEED_METADATA SET INDEX = :value WHERE ID = 'OPPFOLGING_FEED';")
                .setParameter("value", value)
                .executeUpdate()
    }


}