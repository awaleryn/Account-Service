package account.admin;


import account.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class AdminResponse {

    private int id;
    private String name;
    private String lastname;
    private String email;
    private List<String> roles;

    public AdminResponse(Account account) {
        this.id = account.getId();
        this.name = account.getName();
        this.lastname = account.getLastname();
        this.email = account.getEmail();
        this.roles = account.getRoles();
    }
}
