package ru.mishapp.budget.data

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Response
import ru.mishapp.budget.data.remote.ProblemDto
import java.io.IOException

/** Ошибка, пригодная для показа пользователю. */
class ApiException(
    override val message: String,
    val status: Int? = null,
    val unauthorized: Boolean = status == 401,
) : Exception(message)

internal val problemJson = Json { ignoreUnknownKeys = true; isLenient = true }

private val problemMediaType = "application/json".toMediaType()

/** Парсит ProblemDetail из тела ответа. */
internal fun parseProblemMessage(response: Response<*>?): String? {
    val raw = runCatching { response?.errorBody()?.string() }.getOrNull() ?: return null
    if (raw.isBlank()) return null
    val problem = runCatching { problemJson.decodeFromString(ProblemDto.serializer(), raw) }.getOrNull()
    return problem?.detail ?: problem?.title
}

/** Оборачивает сетевой вызов, приводя ошибки к [ApiException]. */
internal suspend fun <T> apiCall(block: suspend () -> T): T = try {
    block()
} catch (exception: HttpException) {
    val status = exception.code()
    val detail = parseProblemMessage(exception.response())
        ?: defaultMessage(status)
    throw ApiException(detail, status)
} catch (exception: IOException) {
    throw ApiException("Нет соединения с сервером. Проверьте сеть.", null)
} catch (exception: ApiException) {
    throw exception
} catch (exception: Exception) {
    throw ApiException(exception.message ?: "Неизвестная ошибка", null)
}

internal fun <T> requireBody(response: Response<T>): T {
    if (response.isSuccessful) {
        return response.body() ?: throw ApiException("Пустой ответ сервера", response.code())
    }
    throw ApiException(parseProblemMessage(response) ?: defaultMessage(response.code()), response.code())
}

internal fun requireSuccess(response: Response<Unit>) {
    if (!response.isSuccessful) {
        throw ApiException(parseProblemMessage(response) ?: defaultMessage(response.code()), response.code())
    }
}

private fun defaultMessage(status: Int): String = when (status) {
    400 -> "Некорректные данные"
    401 -> "Требуется вход в приложение"
    403 -> "Недостаточно прав"
    404 -> "Не найдено"
    409 -> "Конфликт данных"
    in 500..599 -> "Ошибка на сервере"
    else -> "Ошибка запроса ($status)"
}
