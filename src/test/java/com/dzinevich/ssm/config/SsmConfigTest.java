package com.dzinevich.ssm.config;

import com.dzinevich.ssm.domain.PaymentEvent;
import com.dzinevich.ssm.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import java.util.UUID;

@SpringBootTest
class SsmConfigTest {

    @Autowired
    private StateMachineFactory<PaymentState, PaymentEvent> factory ;

    @Test
    void testNewStateMachine() {
        StateMachine<PaymentState, PaymentEvent> machine = factory.getStateMachine(UUID.randomUUID());
        machine.start();

        System.out.println("___" + machine.getState());

        machine.sendEvent(PaymentEvent.AUTHORIZE);
        System.out.println("___" + machine.getState());

        machine.sendEvent(PaymentEvent.PRE_AUTHORIZE_APPROVED);
        System.out.println("___" + machine.getState());

        machine.sendEvent(PaymentEvent.PRE_AUTHORIZE_DECLINED);
        System.out.println("___" + machine.getState());
    }
}