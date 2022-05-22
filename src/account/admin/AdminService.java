package account.admin;

import account.Account;
import account.AccountRepository;
import account.auditor.Event;
import account.auditor.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final AccountRepository accountRepository;
    private final EventService eventService;
    private final HttpServletRequest requestHttp;
    private final List<String> existingRoles = List.of("ROLE_USER", "ROLE_ADMINISTRATOR", "ROLE_ACCOUNTANT", "ROLE_AUDITOR");

    @Autowired
    public AdminService(AdminRepository adminRepository, AccountRepository accountRepository, EventService eventService, HttpServletRequest requestHttp) {
        this.adminRepository = adminRepository;
        this.accountRepository = accountRepository;
        this.eventService = eventService;
        this.requestHttp = requestHttp;
    }

    @Transactional
    public Map<String, Object> setAccountRole(AdminRequest request) {
        String requestedRole = "ROLE_" + request.getRole();

        if (!existingRoles.contains(requestedRole)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
        }

        if (adminRepository.findAccountByEmailIgnoreCase(request.getUser().toLowerCase()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }

        Account accountToSet = adminRepository.findAccountByEmailIgnoreCase(request.getUser()).get();

        if (request.getOperation().equals("GRANT")) {
            if (accountToSet.getRoles().contains("ROLE_ADMINISTRATOR") || requestedRole.equals("ROLE_ADMINISTRATOR")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user cannot combine administrative and business roles!");
            }
            List<String> roles = accountToSet.getRoles(); // we're  taking already set SET of roles

            roles.add(requestedRole); // add new role to it
            Collections.reverse(roles);

            accountToSet.setRoles(roles); // change roles to same SET with added new role

            // GRANT_ROLE EVENT
            eventService.saveEvent(new Event(LocalDateTime.now(), "GRANT_ROLE", adminRepository.findFirstByOrderByIdAsc().getEmail(), "Grant role " + requestedRole.split("_")[1] + " to " +  accountToSet.getEmail(), "/api/admin/user/role"));

        } else if (request.getOperation().equals("REMOVE")) {
            List<String> roles = accountToSet.getRoles();
            if (requestedRole.equals("ROLE_ADMINISTRATOR")) { // if request wants to remove ADMINISTRATOR
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!"); // throw
            }else if (!roles.contains(requestedRole)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"The user does not have a role!");
            }else if (roles.size() == 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"The user must have at least one role!");
            }  else { // else we're removing role

                eventService.saveEvent(new Event(LocalDateTime.now(), "REMOVE_ROLE", adminRepository.findFirstByOrderByIdAsc().getEmail(),
                        "Remove role " + request.getRole() + " from " + accountToSet.getEmail(), requestHttp.getRequestURI()));
                roles.remove(requestedRole);
            }
        }


        return Map.of("id", accountToSet.getId(),
                        "name", accountToSet.getName(),
                        "lastname", accountToSet.getLastname(),
                        "email", accountToSet.getEmail(),
                        "roles", accountToSet.getRoles());
    }



    public List<Account> getAccounts() {
        return adminRepository.getAllByOrderByIdAsc();
    }



    public void deleteAccount(String username) {
        Account accountToDelete;
        if (adminRepository.findAccountByEmailIgnoreCase(username).isPresent()) {
            accountToDelete = adminRepository.findAccountByEmailIgnoreCase(username).get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }

        if (accountToDelete.getRoles().contains("ROLE_ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        }

        eventService.saveEvent(new Event(LocalDateTime.now(), "DELETE_USER", adminRepository.findFirstByOrderByIdAsc().getEmail(),
                username, requestHttp.getRequestURI()));
        accountRepository.delete(accountToDelete);
    }


    public Map<String, String> changeAccess(AdminAccessRequest request) {
        Account accountToChange = null;

        if (accountRepository.findAccountByEmailIgnoreCase(request.getUser()).isPresent()) {

            accountToChange = accountRepository.findAccountByEmailIgnoreCase(request.getUser()).get();
        } else {

            throw new UsernameNotFoundException("Username not found");
        }


        if (request.getOperation().equals("UNLOCK")) {

            accountToChange.setLoginFailureCount(0);
            accountToChange.setAccountNonLocked(true);
            accountRepository.save(accountToChange);

            eventService.saveEvent(new Event(LocalDateTime.now(), "UNLOCK_USER", adminRepository.findFirstByOrderByIdAsc().getEmail(),
                    "Unlock user " + accountToChange.getEmail(), requestHttp.getRequestURI()));

            return Map.of("status" , "User " + accountToChange.getEmail() + " unlocked!");
        } else {
            if (!accountToChange.getRoles().contains("ROLE_ADMINISTRATOR")){
                accountToChange.setLoginFailureCount(5);
                accountToChange.setAccountNonLocked(false);
                accountRepository.save(accountToChange);

                eventService.saveEvent(new Event(LocalDateTime.now(), "LOCK_USER", adminRepository.findFirstByOrderByIdAsc().getEmail(),
                        "Lock user " + accountToChange.getEmail(), requestHttp.getRequestURI()));

                return Map.of("status" , "User " + accountToChange.getEmail() + " locked!");
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
            }

        }

    }

}
