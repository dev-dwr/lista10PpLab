package org.example.ex3.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.example.ex3.products.Grapes;
import org.example.ex3.products.WineJuice;

public class EmbossingActor extends AbstractBehavior<EmbossingActor.EmbossingCommand> {

    private final ActorRef<StorageActor.StorageCommand> sharedResource;
    public interface EmbossingCommand {}
    public static class EmbossingMessage implements EmbossingCommand{
        private String text;

        public EmbossingMessage(String text) {
            this.text = text;
        }


    }

    public static class EmbossingStorage implements EmbossingCommand{
        private String text;
        private StorageActor.Storage currentStorage;
        public EmbossingStorage(String text, StorageActor.Storage storage) {
            this.text = text;
            this.currentStorage = storage;
        }

        public String getText() {
            return text;
        }

        public StorageActor.Storage getCurrentStorage() {
            return currentStorage;
        }
    }

    private StorageActor.Storage currentStorage;
    private EmbossingActor(ActorContext<EmbossingCommand> context, ActorRef<StorageActor.StorageCommand> sharedResource) {
        super(context);
        this.sharedResource = sharedResource;
        currentStorage = new StorageActor.Storage();
    }

    private final double failureProb = 0.0;
    private final double timeInH = 12;
    private int slots = 1;

    private int requiredGrapesInKg = 15;

    private WineJuice product = new WineJuice(10);

    @Override
    public Receive<EmbossingCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(EmbossingMessage.class, this::onStartMessage)
                .onMessage(EmbossingStorage.class, this::onGetState)
                .build();
    }


    private final ActorRef<StorageActor.StorageCommand> storage = getContext()
            .spawn(StorageActor.behavior(), "storageActor");

    private Behavior<EmbossingCommand> onStartMessage(EmbossingMessage message) {
//        checkRequiredGrapesAndSlots(receivedStorage);
//        System.out.println("Im here");
//        this.setSlots(0);
//
//        int currentGrapes = receivedStorage.getGrapesKg().getAmountInKg();
//        Grapes updatedGrapes = new Grapes(currentGrapes - requiredGrapesInKg);
//        receivedStorage.setGrapesKg(updatedGrapes);
//        receivedStorage.setWineJuice(product);
//
//        try {
//            Thread.sleep(12 * 1000);
//        }catch (InterruptedException e){}
//        this.setSlots(1);
//
//        sharedResource.tell(receivedStorage);
//        currentStorage.setWineJuice(new WineJuice(10));
        storage.tell(new StorageActor.GetState(getContext().getSelf()));
        return this;
    }

    private Behavior<EmbossingCommand> onGetState(EmbossingStorage message){
        currentStorage = message.getCurrentStorage();
        currentStorage.setGrapesKg(new Grapes(currentStorage.getGrapesKg().getAmountInKg() - 15));
        currentStorage.setWineJuice(new WineJuice(10));
        storage.tell(currentStorage);
        return this;
    }


    public double getTimeInH() {
        return timeInH;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }
//    private void checkRequiredGrapesAndSlots(StorageActor.Storage givenStorage) {
//        if(slots == 0){
//            System.out.println("Slot is taken");
//        }
//        if (givenStorage.getGrapesKg().getAmountInKg() < requiredGrapesInKg) {
//            System.out.println("Not enough grapes");
//        }
//    }

    public static Behavior<EmbossingCommand> behavior(ActorRef<StorageActor.StorageCommand> sharedResource) {
        return Behaviors.setup(context -> new EmbossingActor(context, sharedResource));
    }
}
