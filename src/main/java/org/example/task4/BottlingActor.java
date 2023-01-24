package org.example.task4;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;
import java.util.Random;

public class BottlingActor extends AbstractBehavior<BottlingActor.Command> {
    public interface Command {
    }

    public static class ProvideFilteredWine implements Command {
        private double amount;

        public ProvideFilteredWine(double amount) {
            this.amount = amount;
        }

        public double getFilteredWineAmount() {
            return amount;
        }
    }

    public static class EndOfProcess implements Command {
    }

    public enum Process implements Command {
        OFF
    }

    private boolean resources = true;
    private boolean flag = true;
    private double filteredWine = 0;
    private int bottles;
    private int time5Minutes = 300000;
    private int failure = 5;
    private int createdBottles = 1;
    private int time = 500;
    private int slots = 1;
    private double requiredFilteredWine = 0.75d;
    private ActorRef<StorageActor.Command> storage;
    private int speed;

    public static Behavior<Command> create(ActorRef<StorageActor.Command> storage, ActorRef<FiltrationActor.Command> filtration, int bottles, int speed) {
        return Behaviors.setup(context -> new BottlingActor(context, storage, filtration, bottles, speed));
    }

    private BottlingActor(ActorContext<Command> context, ActorRef<StorageActor.Command> storage, ActorRef<FiltrationActor.Command> filtration, int bottles, int speed) {
        super(context);
        this.storage = storage;
        this.speed = speed;
        this.bottles = bottles;
        getContext().watch(filtration);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ProvideFilteredWine.class, this::provideFilteredWine)
                .onMessage(Process.class, s -> Behaviors.stopped())
                .onMessage(EndOfProcess.class, this::endOfProcessing)

                .onSignal(Terminated.class, signal -> filtrationStop())
                .build();
    }

    private Behavior<Command> endOfProcessing(EndOfProcess msg) {
        slots = 1;
        Random random = new Random();
        int value = random.nextInt(100);

        if (failure > value) {
            System.out.println("Bootling error occurred");
        } else {
            System.out.println("Bottling Success, produced produced bootle: " + createdBottles);
            storage.tell(new StorageActor.ProvideBottles(createdBottles));
        }

        flag = false;
        getContext().getSelf().tell(new BottlingActor.ProvideFilteredWine(filteredWine));
        return this;
    }

    private Behavior<Command> provideFilteredWine(ProvideFilteredWine wine) {

        if(flag){
            filteredWine += wine.getFilteredWineAmount();
        }
        System.out.println("filtered wine bottling process: " + filteredWine);
        if (0 == slots) {
            System.out.println("turning off bottling");
            getContext().getSelf().tell(BottlingActor.Process.OFF);
        }

        while (slots == 1 && filteredWine >= requiredFilteredWine && bottles >= createdBottles) {
            slots = 0;
            filteredWine -= requiredFilteredWine;
            bottles -= createdBottles;
            getContext().scheduleOnce(Duration.ofMillis(time / speed), getContext().getSelf(), new BottlingActor.EndOfProcess());
        }

//        resources = false;
//        if (slots == 1 && !resources) {
//            getContext().getSelf().tell(Process.OFF);
//            storage.tell(StorageActor.ShutdownProduction.SHUTDOWN);
//        }

        return this;
    }

    private Behavior<Command> filtrationStop() {
        resources = false;
        if (slots == 1 && !resources) {
            getContext().getSelf().tell(Process.OFF);
            storage.tell(StorageActor.ShutdownProduction.SHUTDOWN);
        }
        return this;
    }

}
