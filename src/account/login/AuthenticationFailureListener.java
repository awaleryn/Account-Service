package account.login;

import account.Account;
import account.AccountService;
import account.auditor.Event;
import account.auditor.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;


import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;


@Component
public class AuthenticationFailureListener
        implements ApplicationListener <AuthenticationFailureBadCredentialsEvent> {

    private final int MAX_ATTEMPTS = 5;
    private final AccountService accountService;
    private final EventService eventService;
    private final HttpServletRequest request;

    @Autowired
    public AuthenticationFailureListener(AccountService accountService, EventService eventService, HttpServletRequest request) {
        this.accountService = accountService;
        this.eventService = eventService;
        this.request = request;
    }

    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {

        String username = event.getAuthentication().getName();
        Account account = accountService.getAccountByEmail(username);

        if (account != null && account.isAccountNonLocked()) { // if user falsely tries to log to non locked account increase failure count
            int attempts = account.getLoginFailureCount() + 1;
            if (!account.getRoles().contains("ROLE_ADMINISTRATOR")) { // if user is not administrator user set and save
                account.setLoginFailureCount(attempts);
            }
            accountService.checkSave(account);

            if (account.getLoginFailureCount() > MAX_ATTEMPTS-1 && // if user tried falsely too many times and is not administrator lock account
                    !account.getRoles().contains("ROLE_ADMINISTRATOR") &&
                    account.isAccountNonLocked()) {

                account.setAccountNonLocked(false);

                eventService.saveEvent(new Event(LocalDateTime.now(), "LOGIN_FAILED", account.getEmail(),
                        request.getRequestURI(), request.getRequestURI()));

                eventService.saveEvent(new Event(LocalDateTime.now(), "BRUTE_FORCE", account.getEmail(),
                        request.getRequestURI(), request.getRequestURI()));

                eventService.saveEvent(new Event(LocalDateTime.now(), "LOCK_USER", account.getEmail(),
                        "Lock user " + account.getEmail(), request.getRequestURI()));

                accountService.checkSave(account);


            } else {  // save login failed event to repo
                eventService.saveEvent(new Event(LocalDateTime.now(), "LOGIN_FAILED", account.getEmail(), request.getRequestURI(), request.getRequestURI()));

            }
        }
    }

}
