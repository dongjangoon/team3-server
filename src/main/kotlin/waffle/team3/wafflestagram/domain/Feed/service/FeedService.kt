package waffle.team3.wafflestagram.domain.Feed.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import waffle.team3.wafflestagram.domain.Feed.dto.FeedDto
import waffle.team3.wafflestagram.domain.Feed.exception.FeedDoesNotExistException
import waffle.team3.wafflestagram.domain.Feed.model.Feed
import waffle.team3.wafflestagram.domain.Feed.repository.FeedRepository
import waffle.team3.wafflestagram.domain.Tag.model.Tag
import waffle.team3.wafflestagram.domain.Tag.repository.TagRepository
import waffle.team3.wafflestagram.domain.User.exception.UserDoesNotExistException
import waffle.team3.wafflestagram.domain.User.model.User
import waffle.team3.wafflestagram.domain.User.repository.UserRepository
import waffle.team3.wafflestagram.domain.UserTag.model.UserTag
import waffle.team3.wafflestagram.domain.UserTag.repository.UserTagRepository
import waffle.team3.wafflestagram.global.s3.controller.S3Controller
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
class FeedService(
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
    private val userTagRepository: UserTagRepository,
    private val tagRepository: TagRepository,
    private val s3Controller: S3Controller
) {

    @Transactional
    // 사람 태그하기, 위치 추가 기능
    fun upload(uploadRequest: FeedDto.UploadRequest, user: User): Feed {
        val feed = Feed(content = uploadRequest.content, user = user)

        val userTagList = mutableListOf<UserTag>()
        for (nickname in uploadRequest.userTags) {
            val user = userRepository.findByNickname(nickname) ?: throw UserDoesNotExistException("User with this nickname does not exist.")
            val userTag = UserTag(user = user, feed = feed)
            userTagList.add(userTag)
        }

        val tagList = mutableListOf<Tag>()
        for (tag in uploadRequest.tags) {
            val newTag = Tag(content = tag, feed = feed)
            tagList.add(newTag)
        }

        feed.tags = tagList
        feed.userTags = userTagList

        return feedRepository.save(feed)
    }

    @Transactional
    fun update(id: Long, updateRequest: FeedDto.UpdateRequest, user: User): Feed {
        val feed = feedRepository.findByIdOrNull(id)
            ?: throw FeedDoesNotExistException("Feed with this ID does not exist.")

        val userTagList = mutableListOf<UserTag>()
        for (nickname in updateRequest.userTags) {
            val user = userRepository.findByNickname(nickname) ?: throw UserDoesNotExistException("User with this nickname does not exist.")
            val userTag = UserTag(user = user, feed = feed)
            userTagList.add(userTag)
        }

        //  string tag feature
        val tagList = mutableListOf<Tag>()
        for (tag in updateRequest.tags) {
            val newTag = Tag(content = tag, feed = feed)
            tagList.add(newTag)
        }

        for (userTag in feed.userTags) {
            userTagRepository.delete(userTag)
        }
        for (tag in feed.tags) {
            tagRepository.delete(tag)
        }

        feed.apply {
            content = updateRequest.content
            tags = tagList
            userTags = userTagList
            updatedAt = LocalDateTime.now()
        }

        feedRepository.save(feed)

        return feed
    }

    fun get(id: Long): Feed {
        return feedRepository.findByIdOrNull(id)
            ?: throw FeedDoesNotExistException("Feed with this key does not exist.")
    }

    fun getPage(offset: Int, number: Int): Page<Feed> {
        return feedRepository.findByOrderByUpdatedAtDesc(PageRequest.of(offset, number))
    }

    @Transactional
    fun delete(id: Long, user: User) {
        val feed = get(id)
//        val photoKeys = feed.photoKeys.split(",")
//        for (photoKey: String in photoKeys) {
//            s3Controller.deletePhoto(photoKey)
//        }

        feedRepository.delete(feed)
    }
}
