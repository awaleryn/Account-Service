package account.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminRequest {

    @NotBlank
    @NotNull
    private String user;

    @NotBlank
    @NotNull
    private String role;

    @NotBlank
    @NotNull
    private String operation;

}
