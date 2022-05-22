package account.payment;

import account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.*;


@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountService accountService;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, AccountService accountService) {
        this.paymentRepository = paymentRepository;
        this.accountService = accountService;
    }

    @Transactional
    public void create(List<Payment> payments) {
        for (Payment payment : payments) {

            if (!accountService.isAccountExisting(accountService.getAccountByEmail(payment.getEmployee()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account doesn't exist");
            }
        }
        paymentRepository.saveAll(payments);
    }

    public List<PaymentResponse> getAllPayments(String email) {
        List<PaymentResponse> payments = new ArrayList<>();
        for (Payment payment:
                paymentRepository.findAllByEmployee(email.toLowerCase())) {
            payments.add(new PaymentResponse(accountService.getAccountByEmail(email), payment));
        }
        Collections.reverse(payments);
        return payments;
    }

    public Payment getPayment(String email, Date period) {
        return paymentRepository.findByEmployeeAndPeriod(email.toLowerCase(), period).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Payment not found!"));
    }

    public PaymentResponse getPaymentResponse(String email, Date period) {
        return new PaymentResponse(accountService.getAccountByEmail(email), getPayment(email, period));
    }


    public void update(Payment newPayment) {
        accountService.getAccountByEmail(newPayment.getEmployee());
        Payment payment = getPayment(newPayment.getEmployee(), newPayment.getPeriod());
        payment.setSalary(newPayment.getSalary());
        paymentRepository.save(payment);
    }
}

