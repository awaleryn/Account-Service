package account.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@RestController
@RequestMapping("api")
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @NotEmpty(message = "Input payments list cannot be empty.")
    @PostMapping("/acct/payments")
    public Map<String, String> createPayments(@RequestBody
                                              List<@Valid PaymentRequest> paymentRequestList) {
        List<Payment> payments = paymentRequestList.stream().map(Payment::new).collect(Collectors.toList());
        paymentService.create(payments);
        return Map.of("status","Added successfully!");
    }

    @PutMapping("/acct/payments")
    public Map<String, String> updatePayment(@RequestBody @Valid PaymentRequest payment) {
        paymentService.update(new Payment(payment));
        return Map.of("status","Updated successfully!");
    }


    @GetMapping("/empl/payment")
    public Object getPayment(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam @DateTimeFormat(pattern = "MM-yyyy") Optional<Date> period) {

        String email = userDetails.getUsername();

        if (period.isEmpty()) {
            return paymentService.getAllPayments(email);
        } else {
            return paymentService.getPaymentResponse(email, period.get());
        }
    }

}
