package org.example.task4;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;
import java.util.Random;

public class EmbossingActor extends AbstractBehavior<EmbossingActor.Command> {
    public interface Command {
    }

    public static class ProvideGrapes implements Command {
        private int amount;

        public ProvideGrapes(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    public class EndOfProcessing implements Command {

    }

    public enum Process implements Command {
        OFF
    }

    private ActorRef<StorageActor.Command> storage;
    private int requiredGrapesKg = 15;
    private int producedWineJuice = 10;
    private int failure = 0;

    private int receivedGrapes = 0;
    private int slots = 1;
    private long process12HoursInMilliSeconds = 43200000;
    private long process12HoursInMilliSeconds2 = 42000;
    private boolean occurredAddingGrapesOperation = true;
    private int speed;

    private EmbossingActor(ActorContext<Command> context, ActorRef<StorageActor.Command> storage, int speed) {
        super(context);
        this.storage = storage;
        this.speed = speed;
    }

    public static Behavior<Command> create(ActorRef<StorageActor.Command> storage, int speed) {
        return Behaviors.setup(context -> new EmbossingActor(context, storage, speed));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ProvideGrapes.class, this::getGrapes)
                .onMessage(EndOfProcessing.class, this::endOfProcessing)
                .onMessage(Process.class, off -> Behaviors.stopped())
                .build();
    }

    private Behavior<Command> getGrapes(ProvideGrapes grapes) {
        System.out.println("Getting grapes embossing received grapes: " + grapes.getAmount());
        if (occurredAddingGrapesOperation) {
            receivedGrapes += grapes.getAmount();
        }

        if (0 == slots) {
            System.out.println("turning off embossing");
            getContext().getSelf().tell(Process.OFF);
        }

        while (slots == 1 && receivedGrapes >= requiredGrapesKg) {
            slots = 0;
            receivedGrapes -= requiredGrapesKg;
            getContext().scheduleOnce(Duration.ofMillis(process12HoursInMilliSeconds2 / speed), getContext().getSelf(), new EmbossingActor.EndOfProcessing());
        }

        return this;
    }

    private Behavior<Command> endOfProcessing(EndOfProcessing msg) {
        slots = 1;
        Random random = new Random();
        int value = random.nextInt(10);

        if (failure > value) {
            System.out.println("Embossing error occurred");
        } else {
            System.out.println("Embossing Success, produced wine juice in liters: " + producedWineJuice);
            storage.tell(new StorageActor.ProvideWineJuice(producedWineJuice));
        }

        occurredAddingGrapesOperation = false;
        getContext().getSelf().tell(new ProvideGrapes(receivedGrapes));
        return this;
    }

}
