package account;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

@Data
@RequiredArgsConstructor
@Table
public class NewPassword {

    @NotEmpty
    private String new_password;
}
