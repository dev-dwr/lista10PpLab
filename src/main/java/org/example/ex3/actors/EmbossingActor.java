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


    private EmbossingActor(ActorContext<EmbossingCommand> context, ActorRef<StorageActor.StorageCommand> sharedResource) {
        super(context);
        this.sharedResource = sharedResource;
        currentStorage = new StorageActor.Storage();
    }

    private final double failureProb = 0.0;
    private final double timeInH = 12;
    private int slots = 1;

    private int requiredGrapesInKg = 15;
    private StorageActor.Storage currentStorage;

    @Override
    public Receive<EmbossingCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(EmbossingMessage.class, this::onAskStorage)
                .onMessage(EmbossingStorage.class, this::onEmbosse)
                .build();
    }


    private final ActorRef<StorageActor.StorageCommand> storage = getContext()
            .spawn(StorageActor.behavior(), "storageActor");

    private final ActorRef<FermentationActor.FermentationCommand> fermentation = getContext()
            .spawn(FermentationActor.behavior(storage), "fermentationActor");

    private Behavior<EmbossingCommand> onAskStorage(EmbossingMessage message) {
        storage.tell(new StorageActor.GetState(getContext().getSelf()));
        return this;
    }

    private Behavior<EmbossingCommand> onEmbosse(EmbossingStorage message){
        //update storage
        currentStorage = message.getCurrentStorage();
        currentStorage.setGrapesKg(new Grapes(currentStorage.getGrapesKg().getAmountInKg() - 15));
        storage.tell(currentStorage);


        //move to fermentation state
        FermentationActor.EmbossingProduct product = new FermentationActor.EmbossingProduct(new WineJuice(10));
        fermentation.tell(product);
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
