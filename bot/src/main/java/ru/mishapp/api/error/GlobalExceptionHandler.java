package ru.mishapp.api.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice(basePackages = "ru.mishapp.api")
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Ошибка валидации",
                "Запрос содержит некорректные поля");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                errors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage()));
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Ошибка валидации",
                "Запрос содержит некорректные параметры");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class
    })
    public ProblemDetail handleBadRequest(Exception exception) {
        String detail = exception.getMessage() == null
                ? "Запрос содержит некорректные данные"
                : exception.getMessage();
        return problem(HttpStatus.BAD_REQUEST, "Некорректный запрос", detail);
    }

    @ExceptionHandler({NoSuchElementException.class, NoResourceFoundException.class})
    public ProblemDetail handleNotFound(Exception exception) {
        return problem(HttpStatus.NOT_FOUND, "Ресурс не найден", "Запрошенный ресурс не существует");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException exception) {
        return problem(HttpStatus.UNAUTHORIZED, "Требуется авторизация", "Неверный логин или пароль");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException exception) {
        return problem(HttpStatus.FORBIDDEN, "Доступ запрещён", "Недостаточно прав для выполнения операции");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleConflict(DataIntegrityViolationException exception) {
        log.warn("Нарушение целостности данных", exception);
        return problem(HttpStatus.CONFLICT, "Конфликт данных", "Операция нарушает ограничения данных");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException exception) {
        String detail = exception.getReason() == null ? "Ошибка запроса" : exception.getReason();
        return problem(HttpStatus.valueOf(exception.getStatusCode().value()), "Ошибка запроса", detail);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception) {
        log.error("Необработанная ошибка API", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка",
                "Не удалось выполнить запрос");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
