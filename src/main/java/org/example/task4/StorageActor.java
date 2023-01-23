package org.example.task4;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;
import java.time.Instant;

public class StorageActor extends AbstractBehavior<StorageActor.Command> {

    public interface Command {}

    public static class ProvideUnfilteredWine implements Command {
        private int amount;

        public ProvideUnfilteredWine(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    public enum Production implements Command {
        START
    }
    public enum ShutdownProduction implements Command {
        SHUTDOWN
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

    public static class ProvideBottles implements Command {
        private int amount;

        public ProvideBottles(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static class ProvideFilteredWine implements Command {
        private int amount;

        public ProvideFilteredWine(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }
    public static Behavior<StorageActor.Command> start() {
        return Behaviors.setup(StorageActor::new);
    }

    private Instant startTime;
    private ActorRef<org.example.task4.EmbossingActor.Command> embossing;
    private Instant endTime;
    private ActorRef<BottlingActor.Command> bottlingActor;

    private ActorRef<FermentationActor.Command> fermentationActor;
    private ActorRef<FiltrationActor.Command> filtrationActor;

    private int unfilteredWine;
    private int speeded = 20;
    private int grapes = 100;
    private int water = 100;
    private int filteredWine = 0;
    private int bottles = 10;
    private int wineJuice = 0;
    private int sugar = 5;

    private StorageActor(ActorContext<Command> context) {
        super(context);
        this.embossing = context.spawn(org.example.task4.EmbossingActor.create(getContext().getSelf(), speeded), "embossingActor");
        this.fermentationActor = context.spawn(FermentationActor.create(getContext().getSelf(), embossing, water, sugar, speeded), "fermentationActor");
        this.filtrationActor = context.spawn(FiltrationActor.create(getContext().getSelf(), fermentationActor, speeded), "filtrationActor");
        this.bottlingActor = context.spawn(BottlingActor.create(getContext().getSelf(), filtrationActor, bottles, speeded), "bottlingActor");
//
        getContext().watch(embossing);
        getContext().watch(filtrationActor);
        getContext().watch(fermentationActor);
        getContext().watch(bottlingActor);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ProvideFilteredWine.class, this::provideFilteredWine)
                .onSignal(Terminated.class, this::terminatedProcess)
                .onMessage(ProvideBottles.class, this::provideBottles)
                .onMessage(Production.class, this::startProcess)
                .onMessage(ShutdownProduction.class, shutdown -> onShutdown())
                .onMessage(ProvideWineJuice.class, this::provideWineJuice)
                .onMessage(ProvideUnfilteredWine.class, this::provideUnfilteredWine)
                .build();
    }

    private Behavior<Command> startProcess(Production prod) {
        startTime = Instant.now();
        embossing.tell(new EmbossingActor.ProvideGrapes(grapes));
        return this;
    }


    private Behavior<Command> provideWineJuice(ProvideWineJuice givenWineJuice) {
        wineJuice += givenWineJuice.getAmount();
        fermentationActor.tell(new FermentationActor.ProvideWineJuice(wineJuice));
        return this;
    }
    private Behavior<Command> provideUnfilteredWine(ProvideUnfilteredWine wine) {
        unfilteredWine += wine.getAmount();
        filtrationActor.tell(new FiltrationActor.ProvideUnfilteredWine(unfilteredWine));
        return this;
    }

    private Behavior<Command> provideFilteredWine(ProvideFilteredWine wine) {
        filteredWine += wine.getAmount();
        bottlingActor.tell(new BottlingActor.ProvideFilteredWine(filteredWine));
        return this;
    }

    private Behavior<Command> provideBottles(ProvideBottles givenBottles) {
        bottles += givenBottles.getAmount();
        System.out.println("bottle created in storage");
        return this;
    }
    private Behavior<Command> onShutdown() {
        endTime = Instant.now();
        System.out.println("Process has been finished, Summary: Created new Wine: " +  bottles + " Time: " + Duration.between(startTime, endTime).toMillis() * speeded);

        return Behaviors.stopped();
    }

    private Behavior<Command> terminatedProcess(Terminated terminated) {
        System.out.println("Error type: Termination: " + terminated.getRef().path().name());
        return this;
    }
}
