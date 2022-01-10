package waffle.team3.wafflestagram.domain.Feed.dto

import waffle.team3.wafflestagram.domain.Comment.dto.CommentDto
import waffle.team3.wafflestagram.domain.Feed.model.Feed
import waffle.team3.wafflestagram.domain.Photo.dto.PhotoDto
import waffle.team3.wafflestagram.domain.Tag.dto.TagDto
import waffle.team3.wafflestagram.domain.User.dto.UserDto
import waffle.team3.wafflestagram.domain.UserTag.dto.UserTagDto
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class FeedDto {
    data class Response(
        val id: Long,
        val author: UserDto.Response,
        val photos: List<PhotoDto.Response>,
        val content: String,
        val comments: List<CommentDto.Response>,
        val likes: List<UserDto.Response>,
        val likeSum: Int,
        val tags: List<TagDto.Response>,
        val userTags: List<UserTagDto.Response>,
        val createdAt: LocalDateTime?, //  null 이 아니어도 되지 않을까?
        val updatedAt: LocalDateTime?
    ) {
        constructor(feed: Feed) : this(
            id = feed.id,
            author = UserDto.Response(feed.user),
            photos = feed.photos.let { it.map { photo -> PhotoDto.Response(photo) } },
            content = feed.content,
            comments = feed.comments.let { it.map { comment -> CommentDto.Response(comment) } },
            likes = feed.likes.let { it.map { like -> UserDto.Response(like.user) } },
            likeSum = feed.likes.count(),
            tags = feed.tags.let { it.map { tag -> TagDto.Response(tag.content) } },
            userTags = feed.userTags.let { it.map { userTag -> UserTagDto.Response(userTag.user.nickname) } },
            createdAt = feed.createdAt!!.truncatedTo(ChronoUnit.SECONDS),
            updatedAt = feed.updatedAt!!.truncatedTo(ChronoUnit.SECONDS),
        )
    }

    data class UploadRequest(
        val content: String,
        val imageKeys: List<String>,
        val tags: List<String>,
        val userTags: List<String>
    )

    data class UpdateRequest(
        val content: String,
        val imageKeys: List<String>,
        val tags: List<String>,
        val userTags: List<String>
    )
}
