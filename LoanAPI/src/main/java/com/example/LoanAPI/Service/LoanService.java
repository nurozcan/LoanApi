package com.example.LoanAPI.Service;

import com.example.LoanAPI.Model.Customer;
import com.example.LoanAPI.Model.Loan;
import com.example.LoanAPI.Model.LoanInstallment;
import com.example.LoanAPI.Model.PaymentResults;
import com.example.LoanAPI.Repository.CustomerRepository;
import com.example.LoanAPI.Repository.LoanInstallmentRepository;
import com.example.LoanAPI.Repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanInstallmentRepository loanInstallmentRepository;

    public Loan createLoan(Long customerId, double amount, double interestRate, int numberOfInstallments) {

        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("Invalid customer ID"));

        double totalLoanAmount = amount * (1 + interestRate);

        // Check credit limits
        if (customer.getUsedCreditLimit() + totalLoanAmount > customer.getCreditLimit()) {
            throw new IllegalArgumentException("Credit limit exceeded");
        }

        // save the credit
        Loan loan = new Loan();
        loan.setId(customerId);
        loan.setLoanAmount(totalLoanAmount);
        loan.setNumberOfInstallments(numberOfInstallments);
        loan.setCreateDate(LocalDate.now());
        loan.setPaid(false);
        loanRepository.save(loan);

        // Create installments
        double installmentAmount = totalLoanAmount / numberOfInstallments;

        List<LoanInstallment> installments = new ArrayList<>();
        for (int i = 1; i <= numberOfInstallments; i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setId(loan.getId());
            installment.setAmount(installmentAmount);
            installment.setPaidAmount(0);
            installment.setDueDate(LocalDate.now().plusMonths(i).withDayOfMonth(1));
            installment.setPaid(false);
            installments.add(installment);
        }
        loanInstallmentRepository.saveAll(installments);

        customer.setUsedCreditLimit(Double.sum(customer.getUsedCreditLimit(), totalLoanAmount));
        customerRepository.save(customer);

        return loan;
    }

    public Optional<Loan> listLoanByCutomer(Long customerId) {
        return loanRepository.findById(customerId);
    }

    // List the installments
    public List<LoanInstallment> listInstallmentsByLoanId(Long loanId) {
        return loanInstallmentRepository.findByLoanIdAndIsPaidFalseOrderByDueDateAsc(loanId);
    }

    // Pay loan installments
    public PaymentResults payLoanInstallments(Long loanId, BigDecimal amount) {
        List<LoanInstallment> installments = listInstallmentsByLoanId(loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        double totalPaid = 0;
        int installmentsPaid = 0;

        for (LoanInstallment installment : installments) {
            if (installment.isPaid()) continue;

            if (installment.getDueDate().isAfter(LocalDate.now().plusMonths(3))) break;

            double installmentAmount = installment.getAmount() - installment.getPaidAmount();

            if (amount.compareTo(BigDecimal.valueOf(installmentAmount)) >= 0) {
                // Installments can pay totally
                amount = amount.subtract(BigDecimal.valueOf(installmentAmount));
                installment.setPaidAmount(installmentAmount);
                installment.setPaid(true);
                installment.setPaymentDate(LocalDate.now());
                loanInstallmentRepository.save(installment);

                totalPaid = totalPaid + installmentAmount;
                installmentsPaid++;
            } else {
                break;
            }
        }

        // Totally paid?
        boolean loanIsFullyPaid = installments.stream().allMatch(LoanInstallment::isPaid);
        loan.setPaid(loanIsFullyPaid);
        loanRepository.save(loan);

        return new PaymentResults(installmentsPaid, totalPaid, loanIsFullyPaid);
    }
}
