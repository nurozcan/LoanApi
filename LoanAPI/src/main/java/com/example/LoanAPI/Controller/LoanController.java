package com.example.LoanAPI.Controller;

import com.example.LoanAPI.Model.Loan;
import com.example.LoanAPI.Service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/create")
    public Loan createLoan(@RequestParam Long customerId, @RequestParam double amount,
                           @RequestParam double interestRate, @RequestParam int numOfInstallments) {
        return loanService.createLoan(customerId, amount, interestRate, numOfInstallments);
    }

    @GetMapping("/{customerId}")
    public Optional<Loan> getLoans(@PathVariable Long customerId) {
        return loanService.listLoanByCutomer(customerId);
    }
}
