package backend.academy.scrapper.exception;

public class BotServerException extends InternalServerException {

    public BotServerException(String message) {
        super(message);
    }
}
