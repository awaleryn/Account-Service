package account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED,
        reason = "User account is locked!")
public class AccountLockedException  extends RuntimeException {

    public AccountLockedException()  {
        super();
    }

    public AccountLockedException(String message)  {
        super(message);
    }
}
