package account.login;

import account.Account;
import account.AccountService;
import account.auditor.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;


@Component
public class AuthenticationSuccessListener
        implements ApplicationListener <AuthenticationSuccessEvent> {

    @Autowired
    private final AccountService accountService;

    public AuthenticationSuccessListener(AccountService accountService) {
        this.accountService = accountService;
    }

    public void onApplicationEvent(AuthenticationSuccessEvent event) {

        Account accountToCheck = accountService.getAccountByEmail(event.getAuthentication().getName());

        accountToCheck.setLoginFailureCount(0);
        accountService.checkSave(accountToCheck);
    }
}

