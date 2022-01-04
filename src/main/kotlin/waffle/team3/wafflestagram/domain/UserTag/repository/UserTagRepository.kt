package waffle.team3.wafflestagram.domain.UserTag.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import waffle.team3.wafflestagram.domain.UserTag.model.UserTag

@Repository
interface UserTagRepository : JpaRepository<UserTag, Long>
