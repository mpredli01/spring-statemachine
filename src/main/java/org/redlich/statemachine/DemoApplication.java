package org.redlich.statemachine;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;

import java.util.ArrayList;
import java.util.EnumSet;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    static enum States {
        LOCKED,
        UNLOCKED
        }

    static enum Events {
        INSERTTOKEN,
        PASSTHRU
        }

    @Autowired
    StateMachine<States,Events> stateMachine;

    @Override
    public void run(String... args) throws Exception {
        StateMachineListener listener = new StateMachineListener();
        stateMachine.addStateListener(listener);
        stateMachine.start();
        stateMachine.sendEvent(Events.INSERTTOKEN);
        stateMachine.sendEvent(Events.PASSTHRU);
        listener.print();
        }

    @Configuration
    @EnableStateMachine
    static class Config extends EnumStateMachineConfigurerAdapter<States,Events> {

        @Override
        public void configure(StateMachineStateConfigurer<States,Events> states)
                throws Exception {
            states
                    .withStates()
                        .initial(States.LOCKED)
                    .states(EnumSet.allOf(States.class));
            }

        @Override
        public void configure(StateMachineTransitionConfigurer<States,Events> transitions)
                throws Exception {
            transitions
                    .withExternal()
                        .source(States.LOCKED)
                        .target(States.UNLOCKED)
                        .event(Events.INSERTTOKEN)
                        .and()
                    .withExternal()
                        .source(States.UNLOCKED)
                        .target(States.LOCKED)
                        .event(Events.PASSTHRU);
            }
        }

    static class StateMachineListener extends StateMachineListenerAdapter<States,Events> {

        final ArrayList<State<States,Events>> statesEntered = new ArrayList<>();
        final ArrayList<Transition<States,Events>> transitionsEntered = new ArrayList<>();

        @Override
        public void stateEntered(State<States,Events> state) {
            statesEntered.add(state);
            }

        @Override
        public void transition(Transition<States,Events> transition) {
            transitionsEntered.add(transition);
            }

        @Override
        public void stateChanged(State<States,Events> from,State<States,Events> to) {
            System.out.println("State changed to: " + to.getId());
            }

        void print() {
            System.out.println("-- Summary of States and Transitions --");
            for(State state : statesEntered) {
                System.out.println("State: " + state.getId());
                }
            for(Transition transition : transitionsEntered) {
                System.out.println("Transition: " + transition.getKind());
                }
            }
        }

    public static void main(String[] args) {
        SpringApplication.run(org.redlich.statemachine.DemoApplication.class,args);
        }
    }
