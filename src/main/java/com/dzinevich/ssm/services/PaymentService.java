package com.dzinevich.ssm.services;

import com.dzinevich.ssm.domain.Payment;
import com.dzinevich.ssm.domain.PaymentEvent;
import com.dzinevich.ssm.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {
    Payment newPayment(Payment payment);
    StateMachine<PaymentState, PaymentEvent> preAuthorizePayment(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> declineAuthorizePayment(Long paymentId);
}
