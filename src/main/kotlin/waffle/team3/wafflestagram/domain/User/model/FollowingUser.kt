package waffle.team3.wafflestagram.domain.User.model

import waffle.team3.wafflestagram.domain.model.BaseTimeEntity
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "following_users")
class FollowingUser(
    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,
) : BaseTimeEntity()
