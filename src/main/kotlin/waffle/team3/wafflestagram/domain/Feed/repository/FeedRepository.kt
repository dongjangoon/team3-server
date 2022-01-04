package waffle.team3.wafflestagram.domain.Feed.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import waffle.team3.wafflestagram.domain.Feed.model.Feed

@Repository
interface FeedRepository : JpaRepository<Feed, Long> {

    fun findByOrderByUpdatedAtDesc(pageable: Pageable): Page<Feed>
}
