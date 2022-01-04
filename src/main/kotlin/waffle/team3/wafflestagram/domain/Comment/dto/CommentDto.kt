package waffle.team3.wafflestagram.domain.Comment.dto

import waffle.team3.wafflestagram.domain.Comment.model.Comment
import waffle.team3.wafflestagram.domain.Reply.dto.ReplyDto
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank

class CommentDto {
    data class Response(
        val id: Long,
        val createdAt: LocalDateTime?,
        val updatedAt: LocalDateTime? = null,
        val writer: String,
        val text: String,
        val reply: List<ReplyDto.Response>? = null,
    ) {
        constructor(comment: Comment) : this(
            id = comment.id,
            createdAt = comment.createdAt,
            updatedAt = comment.updatedAt,
            writer = comment.writer,
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
