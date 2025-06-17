package backend.academy.scrapper.exception;

public class ThirdPartyServerException extends InternalServerException {

    public ThirdPartyServerException(String message) {
        super(message);
    }
}
