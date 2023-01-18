package org.example.ex2;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Hello extends AbstractBehavior<P> {

    public Hello(ActorContext<P> context){
        super(context);
    }

    @Override
    public Receive<P> createReceive() {
        return newReceiveBuilder()
                .onMessage(P.class, this::testMethod)
                .build();
    }

    private Behavior<P> testMethod(P msg){
        System.out.println("test msg is sent to" + msg.getText());
        return this;
    }

    public static Behavior<P> behavior(){
        return Behaviors.setup(Hello::new);
    }
}
