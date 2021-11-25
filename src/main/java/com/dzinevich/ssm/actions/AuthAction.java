package com.dzinevich.ssm.actions;

import com.dzinevich.ssm.domain.PaymentEvent;
import com.dzinevich.ssm.domain.PaymentState;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Random;

import static com.dzinevich.ssm.services.PaymentServiceImpl.PAYMENT_ID_HEADER;

@Component
public class AuthAction implements Action<PaymentState, PaymentEvent> {

    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> context) {
        System.out.println("AUTH CALL");

        PaymentEvent paymentEvent = PaymentEvent.AUTHORIZE_APPROVED;

        if (new Random().nextInt(10) < 8) {
            System.out.println("APPROVED");
        } else {
            System.out.println("DECLINED");
            paymentEvent = PaymentEvent.AUTHORIZE_DECLINED;
        }

        context.getStateMachine().sendEvent(MessageBuilder
                .withPayload(paymentEvent)
                .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
                .build()
        );
    }
}
