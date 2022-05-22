package account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST,
                reason = "The passwords must be different!")
public class SamePasswordException extends RuntimeException {

    public SamePasswordException() {
        super();
    }
}
