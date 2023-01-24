package org.example.task4;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    private int time = 5000;
    private int slots = 1;
    private double requiredFilteredWine = 0.75d;
    private ActorRef<StorageActor.Command> storage;
    private int speed;

    private Map<Integer, Boolean> takenSlots = new HashMap<>();
    private List<Integer> slotsFree = new LinkedList<>();
    private Random random = new Random();

    public static Behavior<Command> create(ActorRef<StorageActor.Command> storage, ActorRef<FiltrationActor.Command> filtration, int bottles, int speed) {
        return Behaviors.setup(context -> new BottlingActor(context, storage, filtration, bottles, speed));
    }

    private BottlingActor(ActorContext<Command> context, ActorRef<StorageActor.Command> storage, ActorRef<FiltrationActor.Command> filtration, int bottles, int speed) {
        super(context);
        this.storage = storage;
        this.speed = speed;
        this.bottles = bottles;
        getContext().watch(filtration);

        takenSlots.put(0, false);
        slotsFree.add(0);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(EndOfProcess.class, this::endOfProcessing)
                .onMessage(ProvideFilteredWine.class, this::provideFilteredWine)
                .onMessage(Process.class, s -> Behaviors.stopped())
                .onSignal(Terminated.class, signal -> filtrationStop())
                .build();
    }

    private Behavior<Command> endOfProcessing(EndOfProcess msg) {
        System.out.println("here");
        Random random = new Random();
        slotsFree.add(0);

        int value = random.nextInt(100) + 1;

        if (failure > value) {
            System.out.println("Bootling error occurred");
        } else {
            System.out.println("Bottling Success, produced produced bootle: " + createdBottles);
            storage.tell(new StorageActor.ProvideBottles(createdBottles));
        }

        if (!resources) {
            getContext().getSelf().tell(Process.OFF);
            storage.tell(StorageActor.ShutdownProduction.SHUTDOWN);
        }

        flag = false;
        getContext().getSelf().tell(new BottlingActor.ProvideFilteredWine(filteredWine));
        return this;
    }

    private Behavior<Command> provideFilteredWine(ProvideFilteredWine wine) {

        filteredWine += wine.getFilteredWineAmount();
        boolean freeSlotsPresent = takenSlots.values().stream().allMatch(x -> x);
        System.out.println("bottling process: " + filteredWine);
//        if (!freeSlotsPresent) {
//            System.out.println("turning off bottling");
//            getContext().getSelf().tell(BottlingActor.Process.OFF);
//        }


        while (!freeSlotsPresent && filteredWine >= requiredFilteredWine && bottles >= createdBottles) {
            filteredWine -= requiredFilteredWine;
            bottles -= createdBottles;
            takenSlots.put(0, true);
            slotsFree.remove(0);
            getContext().getSelf().tell(new BottlingActor.EndOfProcess());
            getContext().scheduleOnce(Duration.ofSeconds(time), getContext().getSelf(), new BottlingActor.EndOfProcess());
        }
        storage.tell(StorageActor.ShutdownProduction.SHUTDOWN);
        return this;
    }

    private Behavior<Command> filtrationStop() {
        resources = false;
        if (slots == slotsFree.size() && !resources) {
            getContext().getSelf().tell(Process.OFF);
            storage.tell(StorageActor.ShutdownProduction.SHUTDOWN);
        }
        return this;
    }

}
