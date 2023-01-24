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

public class FiltrationActor extends AbstractBehavior<FiltrationActor.Command> {
    public interface Command {
    }

    public static class ProvideUnfilteredWine implements Command {
        private int amount;

        public ProvideUnfilteredWine(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static class EndOfProcessing implements Command {
        private int slot;

        public EndOfProcessing(int slot) {
            this.slot = slot;
        }

        public int getSlot() {
            return slot;
        }
    }


    public static Behavior<Command> create(ActorRef<StorageActor.Command> storage, ActorRef<FermentationActor.Command> fermentationActor, int speed) {
        return Behaviors.setup(context -> new FiltrationActor(context, storage, fermentationActor, speed));
    }

    public enum Process implements Command {
        OFF
    }

    private ActorRef<StorageActor.Command> storage;
    private int slots = 10;

    private int unfilteredWine = 0;
    private int requiredUnfilteredWine = 25;
    private int producedFilteredWine = 24;
    private int filteredWine = 24;
    private int failure = 0;
    private Map<Integer, Boolean> takenSlots = new HashMap<>();
    private List<Integer> slotsFree = new LinkedList<>();
    private boolean resources = true;
    private int speed;

    private FiltrationActor(ActorContext<Command> context, ActorRef<StorageActor.Command> storage, ActorRef<FermentationActor.Command> fermentation, int speed) {
        super(context);
        this.speed = speed;
        this.storage = storage;
        getContext().watch(fermentation);
        for (int i = 0; i < slots; i++) {
            takenSlots.put(i, false);
            slotsFree.add(i);
        }
    }

//    private long timeInMilliSeconds = 1209600000; //14dni
    private long timeInMilliSeconds = 7200; //14dni
    private int time = 432000;
    ;
    private Random random = new Random();

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ProvideUnfilteredWine.class, this::provideUnfilteredWine)
                .onMessage(EndOfProcessing.class, this::endOfProcessing)
                .onMessage(Process.class, off -> Behaviors.stopped())
                .onSignal(Terminated.class, signal -> endFermentationProcessDueToLackOfResources())
                .build();
    }

    private Behavior<Command> endFermentationProcessDueToLackOfResources() {
        resources = false;
        if (slotsFree.size() == slots && !resources) {
            getContext().getSelf().tell(Process.OFF);
        }
        return this;
    }


    private Behavior<Command> provideUnfilteredWine(ProvideUnfilteredWine wine) {
        System.out.println("provided unfiltered wine: " + wine.getAmount());
        unfilteredWine += wine.getAmount();

        boolean freeSlotsPresent = takenSlots.values().stream().allMatch(x -> x);
        while (unfilteredWine >= requiredUnfilteredWine && !freeSlotsPresent) {
            unfilteredWine -= requiredUnfilteredWine;
            int randomSlot = random.nextInt(slots);
            while (takenSlots.get(randomSlot)) {
                randomSlot = random.nextInt(slots);
            }
            takenSlots.put(randomSlot, true);
            slotsFree.remove(randomSlot);

            getContext().getSelf().tell(new FiltrationActor.EndOfProcessing(randomSlot));
            getContext().scheduleOnce(Duration.ofMillis(timeInMilliSeconds/speed), getContext().getSelf(), new FiltrationActor.EndOfProcessing(randomSlot));
        }

        if (slotsFree.size() == slots && !resources) {
            getContext().getSelf().tell(Process.OFF);
        }

        return this;
    }


    private Behavior<Command> endOfProcessing(EndOfProcessing msg) {
        System.out.println("Filtration Process has been finished of slot: " + msg.getSlot());
        slotsFree.add(msg.getSlot());

        if (random.nextInt(100) + 1 < failure) {
            System.out.println("Filtration of slot id: " + msg.getSlot() + " has failed");
        } else {
            System.out.println("Filtration succeeded, produced filtered wine to storage: " + filteredWine + " next step bootling");
            storage.tell(new StorageActor.ProvideFilteredWine(filteredWine));
        }

        getContext().getSelf().tell(new FiltrationActor.ProvideUnfilteredWine(producedFilteredWine));
        return this;
    }


}
