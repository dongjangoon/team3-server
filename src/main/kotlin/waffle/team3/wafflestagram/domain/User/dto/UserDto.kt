package waffle.team3.wafflestagram.domain.User.dto

import waffle.team3.wafflestagram.domain.User.model.User
import javax.validation.constraints.NotBlank

class UserDto {
    data class Response(
        val id: Long,
        val email: String,
        val name: String? = null,
        val nickname: String? = null,
        val public: Boolean = true,
        val website: String? = null,
        val bio: String? = null,
    ) {
        constructor(user: User) : this(
            id = user.id,
            email = user.email,
            name = user.name,
            nickname = user.nickname,
            public = user.public,
            website = user.website,
            bio = user.bio,
        )
    }
    data class SignupRequest(
        @field:NotBlank
        val email: String,

        @field:NotBlank
        val password: String,

        val name: String? = null,
        val nickname: String? = null,
        val public: Boolean = true,
        val website: String? = null,
        val bio: String? = null,
    )
    data class ProfileRequest(
        val public: Boolean? = null,
        val name: String? = null,
        val nickname: String? = null,
        val website: String? = null,
        val bio: String? = null,
    )
}
