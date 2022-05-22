package account.payment;

import account.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Locale;

@NoArgsConstructor
@Getter
@Setter
public class PaymentResponse {

    private String name;
    private String lastname;
    private String period;
    private String salary;

    public PaymentResponse(Account account, Payment payment) {
        this.name = account.getName();
        this.lastname = account.getLastname();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM-yyyy", Locale.US);
        this.period = simpleDateFormat.format(payment.getPeriod());
        this.salary = String.format("%d dollar(s) %d cent(s)", payment.getSalary() / 100, payment.getSalary() % 100);
    }


}
