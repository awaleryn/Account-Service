package account;

import account.auditor.Event;
import account.auditor.EventService;
import account.exception.AccountLockedException;
import account.exception.BreachedPasswordException;
import account.exception.PasswordLengthIncorrectException;
import account.exception.SamePasswordException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventService eventService;
    private final HttpServletRequest request;

    private final Set<String> breachedPasswords = Set.of("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    @Autowired
    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder, EventService eventService, HttpServletRequest request) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventService = eventService;
        this.request = request;
    }

    protected void register(Account account) {  // register new account and encode password
        if (breachedPasswords.contains(account.getPassword())) {
            throw new BreachedPasswordException();
        }

        if (account.getPassword().length() < 12) {
            throw new PasswordLengthIncorrectException();
        }

        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setEmail(account.getEmail().toLowerCase());
        accountRepository.save(account);

        updateRoles(account);  // after saving account we can find it by Id and update it's roles
        accountRepository.delete(account); // then we delete original account without roles

        accountRepository.save(account); // and save same account but updated with roles

        //CREATE_USER EVENT
        eventService.saveEvent(new Event(LocalDateTime.now(), "CREATE_USER", "Anonymous", account.getEmail(), "/api/auth/signup"));

    }

    public boolean isAccountExisting(Account account) {  // check if account is already in database
        return accountRepository.findAccountByEmailIgnoreCase(account.getEmail()).isPresent();
    }

    public Account getAccountByEmail(String email) {

        if (accountRepository.findAccountByEmailIgnoreCase(email).isPresent()) {
            return accountRepository.findAccountByEmailIgnoreCase(email).get();
        } else {
            eventService.saveEvent(new Event(LocalDateTime.now(), "LOGIN_FAILED", email, request.getRequestURI(), request.getRequestURI()));
            throw new UsernameNotFoundException("Username not found");
        }

    }


    @Transactional
    public Map<String, Object> changePassword(NewPassword newPassword, UserDetails details) {
        String username = details.getUsername(); // getting logged username

        if (isPasswordSame(details, newPassword.getNew_password())) {
            throw new SamePasswordException();
        } else if (breachedPasswords.contains(newPassword.getNew_password())){
            throw new BreachedPasswordException();
        } else if (newPassword.getNew_password().length() < 12) {
            throw new PasswordLengthIncorrectException();
        } else {

            Account accountToChange = accountRepository.findAccountByEmailIgnoreCase(username).get();

            if (!accountToChange.isAccountNonLocked()) {
                throw new AccountLockedException("User account is locked!");
            }

            accountToChange.setPassword(passwordEncoder.encode(newPassword.getNew_password()));

            eventService.saveEvent(new Event(LocalDateTime.now(), "CHANGE_PASSWORD", accountToChange.getEmail(),
                    accountToChange.getEmail(), request.getRequestURI()));

            return Map.of(
                    "email", details.getUsername().toLowerCase(),
                    "status", "The password has been updated successfully");
        }
    }


    void updateRoles(Account account) {
        List<String> roles = account.getRoles();
        if (account.getId() == 1) {
            roles.add("ROLE_ADMINISTRATOR");
            account.setRoles(roles);
        } else {
            roles.add("ROLE_USER");
            account.setRoles(roles);
        }
    }

    private boolean isPasswordSame(UserDetails details, String newPassword) {
        return passwordEncoder.matches(newPassword,details.getPassword());
    }

    public void checkSave(Account account) {
        accountRepository.save(account);
    }

    public List<Account> getMeAll() {
        return accountRepository.getAllByOrderByIdAsc();
    }
}
