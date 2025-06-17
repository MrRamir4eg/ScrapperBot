package backend.academy.bot.exception;

import backend.academy.bot.dto.response.ApiErrorResponse;
import lombok.Getter;

@Getter
public class ScrapperApiException extends RuntimeException {

    private final ApiErrorResponse error;

    public ScrapperApiException(ApiErrorResponse error) {
        this.error = error;
    }
}
