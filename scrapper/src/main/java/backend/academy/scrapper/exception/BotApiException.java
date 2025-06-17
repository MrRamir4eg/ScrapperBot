package backend.academy.scrapper.exception;

import backend.academy.scrapper.dto.response.ApiErrorResponse;
import lombok.Getter;

@Getter
public class BotApiException extends RuntimeException {

    private final ApiErrorResponse errorResponse;

    public BotApiException(ApiErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }
}
