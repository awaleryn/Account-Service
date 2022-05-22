package account;

import account.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/")

public class AccountRestController {

    private final AccountService accountService;

    @Autowired
    public AccountRestController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("signup")  // registering new User
    Account registerUser(@Valid @RequestBody Account account) {
        boolean exits = accountService.isAccountExisting(account);

        if (exits) {
            throw new UserAlreadyExistsException();
        } else {
            accountService.register(account);
            return account;
        }
    }

    @PostMapping("changepass") // changing password, checking is password breached, checking is password same as previous
    Map<String, Object> changePassword(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody NewPassword new_password) {

        return accountService.changePassword(new_password, userDetails);
    }
}
