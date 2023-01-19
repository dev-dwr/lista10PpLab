package org.example.ex3.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.example.ex3.products.Bottels;
import org.example.ex3.products.Grapes;
import org.example.ex3.products.Suger;
import org.example.ex3.products.Water;
import org.example.ex3.products.WineJuice;

public class StorageActor extends AbstractBehavior<StorageActor.StorageCommand> {

    public interface StorageCommand {
    }

    Water water = new Water(100);
    Grapes grapes = new Grapes(50);
    Suger suger = new Suger(30);
    Bottels bottels = new Bottels(20);

    public static class Storage implements StorageCommand {

        private Water waterLiters;
        private Grapes grapesKg;
        private Suger sugarKg;
        private Bottels bottles;

        private WineJuice wineJuice;


        public Storage() {
        }

        public Storage(Water waterLiters, Grapes grapesKg, Suger sugarKg, Bottels bottles) {
            this.waterLiters = waterLiters;
            this.grapesKg = grapesKg;
            this.sugarKg = sugarKg;
            this.bottles = bottles;
        }

        public WineJuice getWineJuice() {
            return wineJuice;
        }

        public void setWineJuice(WineJuice wineJuice) {
            this.wineJuice = wineJuice;
        }

        public Water getWaterLiters() {
            return waterLiters;
        }

        public void setWaterLiters(Water waterLiters) {
            this.waterLiters = waterLiters;
        }

        public Grapes getGrapesKg() {
            return grapesKg;
        }

        public void setGrapesKg(Grapes grapesKg) {
            this.grapesKg = grapesKg;
        }

        public Suger getSugarKg() {
            return sugarKg;
        }

        public void setSugarKg(Suger sugarKg) {
            this.sugarKg = sugarKg;
        }

        public Bottels getBottles() {
            return bottles;
        }

        public void setBottles(Bottels bottles) {
            this.bottles = bottles;
        }

        @Override
        public String toString() {
            return "Storage{" +
                    "waterLiters=" + waterLiters +
                    ", grapesKg=" + grapesKg +
                    ", sugarKg=" + sugarKg +
                    ", bottles=" + bottles +
                    ", wineJuice=" + wineJuice +
                    '}';
        }
    }

    private StorageActor.Storage currentStorage;
    private StorageActor(ActorContext<StorageCommand> context) {
        super(context);
        currentStorage = new StorageActor.Storage(water, grapes, suger, bottels);
    }


    @Override
    public Receive<StorageCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetState.class, this::getState)
                .onMessage(Storage.class, this::setStorage)
                .build();
    }

    public static class GetState implements StorageCommand {
        public final ActorRef<EmbossingActor.EmbossingCommand> replyTo;
        public Storage storage;

        public GetState(ActorRef<EmbossingActor.EmbossingCommand> replyTo) {
            this.replyTo = replyTo;
        }

        public void setStorage(Storage storage) {
            this.storage = storage;
        }
    }




    private Behavior<StorageCommand> getState(GetState state) {
        System.out.println("state get");
        System.out.println("currstorage" + currentStorage.toString());
        state.replyTo.tell(new EmbossingActor.EmbossingStorage("text", currentStorage));
        return this;
    }


    private Behavior<StorageCommand> setStorage(Storage storage) {
        System.out.println("here");
        currentStorage = storage;
        System.out.println(currentStorage.toString());
        return this;
    }


    public static Behavior<StorageCommand> behavior() {
        return Behaviors.setup(StorageActor::new);
    }

}
