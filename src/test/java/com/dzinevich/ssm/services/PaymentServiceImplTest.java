package com.dzinevich.ssm.services;

import com.dzinevich.ssm.domain.Payment;
import com.dzinevich.ssm.domain.PaymentEvent;
import com.dzinevich.ssm.domain.PaymentState;
import com.dzinevich.ssm.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentRepository paymentRepository;
    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .amount(new BigDecimal("11.29"))
                .build();
    }

    @RepeatedTest(10)
    @Transactional
    void preAuthorizePayment() {
        Payment saved = paymentService.newPayment(payment);
        PaymentState initialState = saved.getState();
        StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.preAuthorizePayment(saved.getId());

        Payment preAuthPayment = paymentRepository.getById(saved.getId());

        assertEquals(PaymentState.NEW, initialState);
        assertEquals(PaymentState.PRE_AUTH, stateMachine.getState().getId());
        assertEquals(PaymentState.PRE_AUTH, preAuthPayment.getState());
    }

    @RepeatedTest(10)
    @Transactional
    void authorizePayment() {
        Payment saved = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> preAuthStateMachine = paymentService.preAuthorizePayment(saved.getId());

        if (preAuthStateMachine.getState().getId() == PaymentState.PRE_AUTH) {
            System.out.println("Payment it Pre-Authorized");
            StateMachine<PaymentState, PaymentEvent> authorizePayment = paymentService.authorizePayment(saved.getId());
            System.out.println("Result of Authorization " + authorizePayment.getState().getId());
        } else {
            System.out.println("Payment is NOT Pre-Authorized");
        }
    }
}