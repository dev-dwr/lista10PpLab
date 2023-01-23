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

public class FermentationActor extends AbstractBehavior<FermentationActor.Command> {
    public interface Command {
    }


    public static class ProvideWineJuice implements Command {
        private int amount;

        public ProvideWineJuice(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static class ProvideWater implements Command {
        private int amount;

        public ProvideWater(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static final class ProvideSugar implements Command {
        private int amount;

        public ProvideSugar(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static final class EndOfProcessing implements Command {
        private int slot;

        public EndOfProcessing(int slot) {
            this.slot = slot;
        }

        public int getSlot() {
            return slot;
        }
    }

    public enum Process implements Command {
        OFF
    }

    private ActorRef<StorageActor.Command> storage;
    private int water;
    private int sugar;
    private int speed;
    private int slots = 10;

    private int wineJuice = 0;

    private int requiredWineJuice = 15;
    private int requiredSugar = 2;
    private int requiredWater = 8;

    private int producedUnfilteredWine = 25;
    private boolean resources = true;
    private Map<Integer, Boolean> takenSlots = new HashMap<>();
    private List<Integer> slotsFree = new LinkedList<>();

    private int time = 1200;

    private Random random = new Random();

    public static Behavior<Command> create(ActorRef<StorageActor.Command> storage, ActorRef<EmbossingActor.Command> embossing, int water, int sugar, int speed) {
        return Behaviors.setup(context -> new FermentationActor(context, storage, embossing, water, sugar, speed));
    }

    private FermentationActor(ActorContext<Command> context, ActorRef<StorageActor.Command> warehouse, ActorRef<EmbossingActor.Command> embossingActor, int water, int sugar, int speed) {
        super(context);
        this.storage = warehouse;
        this.water = water;
        this.speed = speed;
        this.sugar = sugar;

        getContext().watch(embossingActor);

        for (int i = 0; i < slots; i++) {
            takenSlots.put(i, false);
            slotsFree.add(i);
        }
    }

    private int failure = 5;

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ProvideWineJuice.class, this::provideWineJuice)
                .onMessage(ProvideSugar.class, this::provideSugar)
                .onMessage(EndOfProcessing.class, this::endOfProcess)
                .onMessage(Process.class, off -> Behaviors.stopped())
                .onMessage(ProvideWater.class, this::provideWater)
                .onSignal(Terminated.class, s -> stopEmbossingActor())
                .build();
    }

    private Behavior<Command> provideWineJuice(ProvideWineJuice msg) {
        wineJuice += msg.getAmount();
        startProcessing();

        return this;
    }

    private Behavior<Command> provideWater(ProvideWater msg) {
        water += msg.getAmount();
        startProcessing();

        return this;
    }

    private Behavior<Command> provideSugar(ProvideSugar msg) {
        sugar += msg.getAmount();
        startProcessing();

        return this;
    }

    private Behavior<Command> endOfProcess(EndOfProcessing msg) {
        System.out.println("Fermentation Process has been finished of slot: " + msg.getSlot());
        slotsFree.add(msg.getSlot());

        if (random.nextInt(100) < failure) {
            System.out.println("Filtration of slot id: " + msg.getSlot() + " has failed");
        } else {
            System.out.println("Fermentation succeeded, produced unfiltered wine to storage: " + producedUnfilteredWine + " next step filtration");
            storage.tell(new StorageActor.ProvideUnfilteredWine(producedUnfilteredWine));
        }

        getContext().getSelf().tell(new FermentationActor.ProvideWater(water));
        getContext().getSelf().tell(new FermentationActor.ProvideSugar(sugar));
        getContext().getSelf().tell(new FermentationActor.ProvideWineJuice(wineJuice));
        return this;
    }

    private void startProcessing() {
        boolean freeSlotsPresent = takenSlots.values().stream().allMatch(x -> x);
        while (wineJuice >= requiredWineJuice && water >= requiredWater && sugar >= requiredSugar && !freeSlotsPresent) {
            water -= requiredWater;
            wineJuice -= requiredWineJuice;
            sugar -= requiredSugar;

            int randomSlot = random.nextInt(slots);
            while (takenSlots.get(randomSlot)) {
                randomSlot = random.nextInt(slots);
            }

            takenSlots.put(randomSlot, true);
            slotsFree.remove(randomSlot);
            getContext().scheduleOnce(Duration.ofMillis(time / speed), getContext().getSelf(), new FermentationActor.EndOfProcessing(randomSlot));
        }

        if (slotsFree.size() == slots && !resources) {
            getContext().getSelf().tell(Process.OFF);
        }
    }

    private Behavior<Command> stopEmbossingActor() {
        resources = false;
        if (slotsFree.size() == slots && !resources) {
            getContext().getSelf().tell(Process.OFF);
        }

        return this;
    }
}
