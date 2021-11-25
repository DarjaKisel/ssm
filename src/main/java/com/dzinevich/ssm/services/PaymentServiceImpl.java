package com.dzinevich.ssm.services;

import com.dzinevich.ssm.domain.Payment;
import com.dzinevich.ssm.domain.PaymentEvent;
import com.dzinevich.ssm.domain.PaymentState;
import com.dzinevich.ssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PaymentServiceImpl implements PaymentService {
    public static final String PAYMENT_ID_HEADER = "payment_id";
    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateChangeIntercentor paymentStateChangeIntercentor;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuthorizePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);
        sendEvent(paymentId, stateMachine, PaymentEvent.PRE_AUTHORIZE);

        return stateMachine;
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);
        sendEvent(paymentId, stateMachine, PaymentEvent.AUTHORIZE);

        return stateMachine;
    }

    @Deprecated
    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuthorizePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);
        sendEvent(paymentId, stateMachine, PaymentEvent.AUTHORIZE_DECLINED);

        return stateMachine;
    }

    private void sendEvent(Long paymentId,
                           StateMachine<PaymentState, PaymentEvent> stateMachine,
                           PaymentEvent paymentEvent) {
        Message<PaymentEvent> msg = MessageBuilder.withPayload(paymentEvent)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();
        stateMachine.sendEvent(msg);

    }

    public StateMachine<PaymentState, PaymentEvent> build(Long paymentId) {
        var payment = paymentRepository.getById(paymentId);
        var machineId = Long.toString(payment.getId());
        var stateMachine = stateMachineFactory.getStateMachine(machineId);

        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(accessor -> {
                    accessor.addStateMachineInterceptor(paymentStateChangeIntercentor);
                    DefaultStateMachineContext<PaymentState, PaymentEvent> defaultStateMachineContext =
                            new DefaultStateMachineContext<>(payment.getState(), null, null, null);
                    accessor.resetStateMachine(defaultStateMachineContext);
                });

        stateMachine.start();

        return stateMachine;
    }
}
