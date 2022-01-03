package waffle.team3.wafflestagram.domain.User.api

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import waffle.team3.wafflestagram.domain.User.dto.FollowerUserDto
import waffle.team3.wafflestagram.domain.User.dto.FollowingUserDto
import waffle.team3.wafflestagram.domain.User.dto.UserDto
import waffle.team3.wafflestagram.domain.User.dto.WaitingFollowerUserDto
import waffle.team3.wafflestagram.domain.User.exception.UserDoesNotExistException
import waffle.team3.wafflestagram.domain.User.model.User
import waffle.team3.wafflestagram.domain.User.service.FollowerUserService
import waffle.team3.wafflestagram.domain.User.service.FollowingUserService
import waffle.team3.wafflestagram.domain.User.service.UserService
import waffle.team3.wafflestagram.domain.User.service.WaitingFollowerUserService
import waffle.team3.wafflestagram.global.auth.CurrentUser
import waffle.team3.wafflestagram.global.auth.JwtTokenProvider
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
    private val followerUserService: FollowerUserService,
    private val followingUserService: FollowingUserService,
    private val waitingFollowerUserService: WaitingFollowerUserService,
    private val jwtTokenProvider: JwtTokenProvider
) {
    @PostMapping("/signup/")
    fun signup(@Valid @RequestBody signupRequest: UserDto.SignupRequest): ResponseEntity<*> {
        val user = userService.signup(signupRequest)
        return ResponseEntity.ok().header("Authentication", jwtTokenProvider.generateToken(user.email)).body(null)
    }

    @GetMapping("/me/")
    fun getMe(@CurrentUser user: User): ResponseEntity<UserDto.Response> {
        return ResponseEntity.ok().body(UserDto.Response(user))
    }

    @PostMapping("/profile/")
    fun setProfile(@CurrentUser user: User, @RequestBody profileRequest: UserDto.ProfileRequest) {
        userService.setProfile(user, profileRequest)
    }

    @GetMapping("/profile/")
    fun getProfile(
        @CurrentUser currentUser: User,
        @RequestParam("nickname") nickname: String
    ): ResponseEntity<UserDto.Response> {
        val user = userService.getUser(currentUser, nickname)
        return ResponseEntity.ok().body(UserDto.Response(user))
    }

    @PostMapping("/follow/{user_id}/")
    @Transactional
    fun followRequest(
        @CurrentUser user: User,
        @PathVariable("user_id") userId: Long,
    ): ResponseEntity<UserDto.Response> {
        val followUser = userService.getUserById(userId)
        if (followUser == null || user.id == userId || user.following.any { it.user.id == userId })
            return ResponseEntity.badRequest().build()
        if (followUser.public) {
            followerUserService.addFollower(followUser, user)
            followingUserService.addFollowing(user, followUser)
            userService.saveUser(user)
            userService.saveUser(followUser)
        } else {
            waitingFollowerUserService.addWaitingFollower(followUser, user)
            userService.saveUser(followUser)
        }
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/unfollow/{user_id}/")
    @Transactional
    fun unfollowRequest(
        @CurrentUser user: User,
        @PathVariable("user_id") userId: Long,
    ): ResponseEntity<UserDto.Response> {
        val followUser = userService.getUserById(userId) ?: throw UserDoesNotExistException("user not exist")
        val erase1 = user.following.removeIf { it.user.id == userId }
        val erase2 = followUser.follower.removeIf { it.user.id == user.id }
        if (erase1 && erase2) {
            userService.saveUser(user)
            userService.saveUser(followUser)
            return ResponseEntity.ok().build()
        }
        return ResponseEntity.badRequest().build()
    }

    @PostMapping("/approve/{user_id}/")
    fun approveRequest(
        @CurrentUser user: User,
        @PathVariable("user_id") userId: Long
    ): ResponseEntity<UserDto.Response> {
        for (waitingFollower in user.waitingFollower) {
            if (waitingFollower.user.id == userId) {
                followingUserService.addFollowing(waitingFollower.user, user)
                followerUserService.addFollower(user, waitingFollower.user)
                user.waitingFollower.remove(waitingFollower)
                userService.saveUser(user)
                return ResponseEntity.ok().build()
            }
        }
        return ResponseEntity.badRequest().build()
    }

    @PostMapping("/refuse/{user_id}/")
    fun refuseRequest(
        @CurrentUser user: User,
        @PathVariable("user_id") userId: Long
    ): ResponseEntity<UserDto.Response> {
        for (waitingFollower in user.waitingFollower) {
            if (waitingFollower.user.id == userId) {
                user.waitingFollower.remove(waitingFollower)
                userService.saveUser(user)
                return ResponseEntity.ok().build()
            }
        }
        return ResponseEntity.badRequest().build()
    }

    @GetMapping("/following/")
    fun getFollowingList(
        @CurrentUser user: User,
        @RequestParam(value = "offset", defaultValue = "0") offset: Int,
        @RequestParam(value = "number", defaultValue = "30") limit: Int,
    ): ResponseEntity<Page<FollowingUserDto.Response>> {
        val result = convertSetToPage(user.following, PageRequest.of(offset, limit))
        return ResponseEntity.ok().body(
            result.map {
                FollowingUserDto.Response(it)
            }
        )
    }

    @GetMapping("/follower/")
    fun getFollowerList(
        @CurrentUser user: User,
        @RequestParam(value = "offset", defaultValue = "0") offset: Int,
        @RequestParam(value = "number", defaultValue = "30") limit: Int,
    ): ResponseEntity<Page<FollowerUserDto.Response>> {
        val result = convertSetToPage(user.follower, PageRequest.of(offset, limit))
        return ResponseEntity.ok().body(
            result.map {
                FollowerUserDto.Response(it)
            }
        )
    }

    @GetMapping("/waiting/")
    fun getWaitingFollowerList(
        @CurrentUser user: User,
        @RequestParam(value = "offset", defaultValue = "0") offset: Int,
        @RequestParam(value = "number", defaultValue = "30") limit: Int,
    ): ResponseEntity<Page<WaitingFollowerUserDto.Response>> {
        val result = convertSetToPage(user.waitingFollower, PageRequest.of(offset, limit))
        return ResponseEntity.ok().body(
            result.map {
                WaitingFollowerUserDto.Response(it)
            }
        )
    }

    fun <E> convertSetToPage(set: MutableSet<E>, pageable: Pageable): Page<E> {
        val list = set.toList()
        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(list.size)
        if (start > list.size) return PageImpl(listOf(), pageable, list.size.toLong())
        return PageImpl(list.subList(start, end), pageable, list.size.toLong())
    }
}