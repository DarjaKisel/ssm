package com.dzinevich.ssm.config;

import com.dzinevich.ssm.actions.*;
import com.dzinevich.ssm.domain.PaymentEvent;
import com.dzinevich.ssm.domain.PaymentState;
import com.dzinevich.ssm.guards.PaymentIdGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

@Slf4j
@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class SsmConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {
    private final PaymentIdGuard paymentIdGuard;
    private final AuthAction authAction;
    private final AuthApprovedAction authApprovedAction;
    private final AuthDeclinedAction authDeclinedAction;
    private final PreAuthAction preAuthAction;
    private final PreAuthApprovedAction preAuthApprovedAction;
    private final PreAuthDeclinedAction preAuthDeclinedAction;

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
         states.withStates()
                 .initial(PaymentState.NEW)
                 .states(EnumSet.allOf(PaymentState.class))
                 .end(PaymentState.AUTH)
                 .end(PaymentState.PRE_AUTH_ERR)
                 .end(PaymentState.AUTH_ERR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions.withExternal()
                .source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE)
                    .action(preAuthAction).guard(paymentIdGuard)
                        .and().withExternal()
                .source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTHORIZE_APPROVED)
                    .action(preAuthApprovedAction)
                        .and().withExternal()
                .source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERR).event(PaymentEvent.PRE_AUTHORIZE_DECLINED)
                    .action(preAuthDeclinedAction)
                //pre_auth state
                        .and().withExternal()
                .source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH).event(PaymentEvent.AUTHORIZE)
                    .action(authAction)
                        .and().withExternal()
                .source(PaymentState.PRE_AUTH).target(PaymentState.AUTH).event(PaymentEvent.AUTHORIZE_APPROVED)
                    .action(authApprovedAction)
                        .and().withExternal()
                .source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERR).event(PaymentEvent.AUTHORIZE_DECLINED)
                    .action(authDeclinedAction);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> machineListenerAdapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info("State change from={} to={}", from, to);
            }
        };

        config.withConfiguration().listener(machineListenerAdapter);
    }

//    public Guard<PaymentState, PaymentEvent> paymentIdGuard() {
//        return context -> {
//            return Objects.nonNull(context.getMessageHeader(PAYMENT_ID_HEADER));
//        };
//    }
//
//    public Action<PaymentState, PaymentEvent> preAuthAction() {
//        return context -> {
//            System.out.println("PREAUTH CALL");
//
//            PaymentEvent paymentEvent = PaymentEvent.PRE_AUTHORIZE_APPROVED;
//
//            if (new Random().nextInt(10) < 8) {
//                System.out.println("APPROVED");
//            } else {
//                System.out.println("DECLINED");
//                paymentEvent = PaymentEvent.PRE_AUTHORIZE_DECLINED;
//            }
//
//            context.getStateMachine().sendEvent(MessageBuilder
//                    .withPayload(paymentEvent)
//                    .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
//                    .build()
//            );
//        };
//    }
//
//    public Action<PaymentState, PaymentEvent> authAction() {
//        return context -> {
//            System.out.println("AUTH CALL");
//
//            PaymentEvent paymentEvent = PaymentEvent.AUTHORIZE_APPROVED;
//
//            if (new Random().nextInt(10) < 8) {
//                System.out.println("APPROVED");
//            } else {
//                System.out.println("DECLINED");
//                paymentEvent = PaymentEvent.AUTHORIZE_DECLINED;
//            }
//
//            context.getStateMachine().sendEvent(MessageBuilder
//                    .withPayload(paymentEvent)
//                    .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
//                    .build()
//            );
//        };
//    }
}
