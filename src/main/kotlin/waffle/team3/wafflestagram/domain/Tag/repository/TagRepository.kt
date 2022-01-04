package waffle.team3.wafflestagram.domain.Tag.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import waffle.team3.wafflestagram.domain.Tag.model.Tag

@Repository
interface TagRepository : JpaRepository<Tag, Long>
