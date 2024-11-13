package com.example.LoanAPI.Model;

public class PaymentResults {

    private int installmentsPaid;
    private double totalAmountPaid;
    private boolean loanFullyPaid;

    public PaymentResults(int installmentsPaid, double totalAmountPaid, boolean loanFullyPaid) {
        this.installmentsPaid = installmentsPaid;
        this.totalAmountPaid = totalAmountPaid;
        this.loanFullyPaid = loanFullyPaid;
    }

    public boolean isLoanFullyPaid() {
        return loanFullyPaid;
    }

    public void setLoanFullyPaid(boolean loanFullyPaid) {
        this.loanFullyPaid = loanFullyPaid;
    }

    public double getTotalAmountPaid() {
        return totalAmountPaid;
    }

    public void setTotalAmountPaid(double totalAmountPaid) {
        this.totalAmountPaid = totalAmountPaid;
    }

    public int getInstallmentsPaid() {
        return installmentsPaid;
    }

    public void setInstallmentsPaid(int installmentsPaid) {
        this.installmentsPaid = installmentsPaid;
    }
}
