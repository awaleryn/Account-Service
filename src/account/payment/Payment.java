package account.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "employee", "period" }) })
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @NotBlank
    private String employee;

    @NotNull
    @Temporal(TemporalType.DATE)
    private Date period;

    @NotNull
    @Min(value = 0, message = "Salary must be non negative!")
    private Long salary;

    public Payment(PaymentRequest paymentRequest) {
        this.employee = paymentRequest.getEmployee();
        SimpleDateFormat formatter = new SimpleDateFormat("MM-yyyy", Locale.ENGLISH);

        try {
            this.period = formatter.parse(paymentRequest.getPeriod());
        } catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Couldn't parse the date");
        }

        this.salary = paymentRequest.getSalary();
    }



}
