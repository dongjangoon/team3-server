package waffle.team3.wafflestagram.domain.Feed.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import waffle.team3.wafflestagram.domain.Feed.dto.FeedDto
import waffle.team3.wafflestagram.domain.Feed.exception.FeedDoesNotExistException
import waffle.team3.wafflestagram.domain.Feed.model.Feed
import waffle.team3.wafflestagram.domain.Feed.repository.FeedRepository
import waffle.team3.wafflestagram.domain.Like.model.Like
import waffle.team3.wafflestagram.domain.Tag.model.Tag
import waffle.team3.wafflestagram.domain.Tag.repository.TagRepository
import waffle.team3.wafflestagram.domain.User.exception.UserDoesNotExistException
import waffle.team3.wafflestagram.domain.User.exception.UserException
import waffle.team3.wafflestagram.domain.User.model.User
import waffle.team3.wafflestagram.domain.User.repository.UserRepository
import waffle.team3.wafflestagram.domain.User.service.FollowingUserService
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
    private val followingUserService: FollowingUserService,
    private val s3Controller: S3Controller
) {

    @Transactional
    // 사람 태그하기, 위치 추가 기능
    fun upload(uploadRequest: FeedDto.UploadRequest, user: User): Feed {
        val feed = Feed(content = uploadRequest.content, user = user)

        val userTagList = mutableListOf<UserTag>()
        for (nickname in uploadRequest.userTags) {
            val findUser = userRepository.findByNickname(nickname)
                ?: throw UserDoesNotExistException("User with this nickname does not exist.")
            if (findUser.id == user.id) throw UserException("You cannot tag yourself.")
            val userTag = UserTag(user = findUser, feed = feed)
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
            val findUser = userRepository.findByNickname(nickname)
                ?: throw UserDoesNotExistException("User with this nickname does not exist.")
            if (findUser.id == user.id) throw UserException("You cannot tag yourself.")
            val userTag = UserTag(user = findUser, feed = feed)
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

    fun getPage(offset: Int, number: Int, user: User): Page<Feed> {
        val feeds = mutableListOf<Feed>()
        val currUser = userRepository.findByIdOrNull(user.id) ?: throw UserDoesNotExistException("")
        for (followingUser in currUser.following) {
            for (f in followingUser.user.feeds) {
                feeds.add(f)
            }
        }
        val sortedFeeds = feeds.sortedBy { it.updatedAt }.reversed()
        return PageImpl(sortedFeeds, PageRequest.of(offset, number), sortedFeeds.size.toLong())
    }

    @Transactional
    fun delete(id: Long, user: User) {
        val feed = feedRepository.findByIdOrNull(id)
            ?: throw FeedDoesNotExistException("Feed with this key does not exist.")
//        val photoKeys = feed.photoKeys.split(",")
//        for (photoKey: String in photoKeys) {
//            s3Controller.deletePhoto(photoKey)
//        }
        feedRepository.delete(feed)
    }

    @Transactional
    fun addLike(id: Long, user: User): Feed {
        val feed = feedRepository.findByIdOrNull(id)
            ?: throw FeedDoesNotExistException("Feed with this key does not exist.")

        val like = Like(feed, user)
        feed.likes.add(like)

        return feed
    }

    @Transactional
    fun deleteLike(id: Long, user: User): Feed {
        val feed = feedRepository.findByIdOrNull(id)
            ?: throw FeedDoesNotExistException("Feed with this key does not exist.")

        val likes = feed.likes
        val deletedLike = likes.find { it.user.id == user.id }
            ?: throw UserDoesNotExistException("You did not add like to this feed.")
        likes.remove(deletedLike)

        feed.likes = likes

        return feed
    }
}
