package com.irctc.service;

import com.irctc.model.Payment;
import com.irctc.model.Ticket;
import com.irctc.model.enums.PaymentStatus;
import com.irctc.pattern.strategy.*;
import com.irctc.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final CreditCardPayment creditCardPayment;
    private final UPIPayment upiPayment;
    private final NetBankingPayment netBankingPayment;
    
    // Manual constructor
    public PaymentService(PaymentRepository paymentRepository,
                          CreditCardPayment creditCardPayment,
                          UPIPayment upiPayment,
                          NetBankingPayment netBankingPayment) {
        this.paymentRepository = paymentRepository;
        this.creditCardPayment = creditCardPayment;
        this.upiPayment = upiPayment;
        this.netBankingPayment = netBankingPayment;
    }
    
    @Transactional
    public Payment processPayment(Ticket ticket, double amount, String method) {
        return processPayment(ticket, amount, method, null);
    }

    @Transactional
    public Payment processPayment(Ticket ticket, double amount, String method, com.irctc.dto.BookingRequest request) {
        String paymentId = "PAY" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Payment payment = new Payment(paymentId, ticket, amount, method);
        
        PaymentStrategy strategy = getPaymentStrategy(method, request);
        
        boolean success = strategy.pay(amount);
        
        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId("TXN" + System.currentTimeMillis());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Transaction declined");
            payment.setRetryCount(payment.getRetryCount() + 1);
        }
        
        return paymentRepository.save(payment);
    }
    
    @Transactional
    public void processRefund(Ticket ticket, double amount) {
        java.util.Optional<Payment> optionalPayment = paymentRepository.findByTicket_Pnr(ticket.getPnr());
        
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            System.out.println("Refund of ₹" + amount + " processed for ticket " + ticket.getPnr());
        } else {
            System.out.println("No payment record found for ticket " + ticket.getPnr() + ". Proceeding with ticket refund status update only.");
        }
    }
    
    private PaymentStrategy getPaymentStrategy(String method, com.irctc.dto.BookingRequest request) {
        if (method == null) method = "creditcard";
        switch (method.toLowerCase()) {
            case "creditcard":
                String card = (request != null && request.getCardNumber() != null && !request.getCardNumber().trim().isEmpty())
                        ? request.getCardNumber() : "4111 1111 1111 1111";
                String exp = (request != null && request.getCardExpiry() != null && !request.getCardExpiry().trim().isEmpty())
                        ? request.getCardExpiry() : "12/28";
                String cvv = (request != null && request.getCardCvv() != null && !request.getCardCvv().trim().isEmpty())
                        ? request.getCardCvv() : "123";
                return creditCardPayment.withDetails(card, exp, cvv);
            case "upi":
                String upi = (request != null && request.getUpiId() != null && !request.getUpiId().trim().isEmpty())
                        ? request.getUpiId() : "passenger@okhdfcbank";
                return upiPayment.withUpiId(upi);
            case "netbanking":
                String bank = (request != null && request.getBankName() != null && !request.getBankName().trim().isEmpty())
                        ? request.getBankName() : "State Bank of India";
                return netBankingPayment.withDetails(bank, "IRCTC_USER");
            default:
                return creditCardPayment.withDetails("4111 1111 1111 1111", "12/28", "123");
        }
    }
    
    public Payment getPaymentByTicket(String pnr) {
        return paymentRepository.findByTicket_Pnr(pnr).orElse(null);
    }
}