package waffle.team3.wafflestagram.global.common.exception

enum class ErrorType(
    val code: Int
) {
    INVALID_REQUEST(0),

    NOT_ALLOWED(3000),

    DATA_NOT_FOUND(4000),
    SURVEY_RESPONSE_NOT_FOUND(4001),
    OS_NOT_FOUND(4002),
    FEED_NOT_FOUND(4003),

    CONFLICT(9000),
    USER_ALREADY_EXISTS(9001),

    SERVER_ERROR(10000)
}