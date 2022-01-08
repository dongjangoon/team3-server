package waffle.team3.wafflestagram.domain.Comment.dto

import waffle.team3.wafflestagram.domain.Comment.model.Comment
import waffle.team3.wafflestagram.domain.Reply.dto.ReplyDto
import waffle.team3.wafflestagram.domain.User.dto.UserDto
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.validation.constraints.NotBlank

class CommentDto {
    data class Response(
        val id: Long,
        val createdAt: LocalDateTime?,
        val updatedAt: LocalDateTime? = null,
        val writer: UserDto.Response,
        val text: String,
        val reply: List<ReplyDto.Response>? = null,
    ) {
        constructor(comment: Comment) : this(
            id = comment.id,
            createdAt = comment.createdAt!!.truncatedTo(ChronoUnit.SECONDS),
            updatedAt = comment.updatedAt!!.truncatedTo(ChronoUnit.SECONDS),
            writer = UserDto.Response(comment.writer),
            text = comment.text,
            reply = comment.replies.filterIndexed { index, i -> index < 3 }
                .let { it.map { reply -> ReplyDto.Response(reply) } }
        )
    }
    data class CreateRequest(
        @field: NotBlank
        val text: String,
    )
    data class UpdateRequest(
        @field: NotBlank
        val text: String,
    )
}
