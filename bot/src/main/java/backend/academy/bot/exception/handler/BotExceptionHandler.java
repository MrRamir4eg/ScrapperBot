package backend.academy.bot.exception.handler;

import backend.academy.bot.dto.response.ApiErrorResponse;
import jakarta.servlet.ServletException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class BotExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, RuntimeException.class, ServletException.class})
    public ResponseEntity<ApiErrorResponse> handleIncorrectRequest(Exception ex) {
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        "Некорректные параметры запроса",
                        String.valueOf(HttpStatus.BAD_REQUEST),
                        ex.getClass().getSimpleName(),
                        ex.getMessage(),
                        Arrays.stream(ex.getStackTrace())
                                .map(StackTraceElement::toString)
                                .toList()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleServerException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        "Произошла ошибка на сервере",
                        String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR),
                        ex.getClass().getSimpleName(),
                        ex.getMessage(),
                        Arrays.stream(ex.getStackTrace())
                                .map(StackTraceElement::toString)
                                .toList()));
    }
}
