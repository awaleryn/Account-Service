package account.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
public class PaymentRequest {

    @NotBlank
    private String employee;

    @NotNull
    @Pattern(regexp="(0[1-9]|1[012])-\\d{4}", message = "Invalid date!")
    private String period;

    @NotNull(message = "Salary must not be null")
    @Min(value = 0, message = "Salary must be non negative!")
    Long salary;


}
