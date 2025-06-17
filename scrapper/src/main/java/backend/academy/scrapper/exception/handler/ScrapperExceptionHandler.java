package backend.academy.scrapper.exception.handler;

import backend.academy.scrapper.dto.response.ApiErrorResponse;
import backend.academy.scrapper.exception.ObjectNotFoundException;
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
public class ScrapperExceptionHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<?> objectNotFound(ObjectNotFoundException e) {
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        e.getMessage(),
                        String.valueOf(HttpStatus.NOT_FOUND),
                        e.getClass().getSimpleName(),
                        e.getMessage(),
                        Arrays.stream(e.getStackTrace())
                                .map(StackTraceElement::toString)
                                .toList()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({RuntimeException.class, MethodArgumentNotValidException.class, ServletException.class})
    public ResponseEntity<ApiErrorResponse> unsupportedOperations(Exception e) {
        return handleBadRequest(e);
    }

    private ResponseEntity<ApiErrorResponse> handleBadRequest(Exception e) {
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        "Некорректные параметры запроса",
                        String.valueOf(HttpStatus.BAD_REQUEST),
                        e.getClass().getSimpleName(),
                        e.getMessage(),
                        Arrays.stream(e.getStackTrace())
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
