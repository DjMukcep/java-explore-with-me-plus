package ru.practicum.controller.private_api;

/**
 * POST /comments/users/{userId}
 * PATCH /comments/{commentId}/users/{userId}
 * DELETE /comments/{commentId}/users/{userId}
 * GET /comments/users/{userId} - получить свои комменты
 * -------------------------------------------------
 * при создании, обовлении коммента проверить что нет бана иначе Conflict
 * обновлять удалять можно тольк свои комменты
 * нельзя комментировать не опубликованое событие
 * Как пользователь пишет 2 коммент то его ранк повышается до REGULAR. 3 и выше VETERAN
 */
public class CommentPrivateController {
}
