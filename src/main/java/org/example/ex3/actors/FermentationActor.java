package org.example.ex3.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.example.ex3.products.Grapes;
import org.example.ex3.products.WineJuice;

public class FermentationActor extends AbstractBehavior<FermentationActor.FermentationCommand> {
    private final ActorRef<StorageActor.StorageCommand> sharedResource;
    private StorageActor.Storage currentStorage;
    public interface FermentationCommand {}

    public static class EmbossingProduct implements FermentationCommand{
        private final WineJuice wineJuice;

        public EmbossingProduct(WineJuice wineJuice) {
            this.wineJuice = wineJuice;
        }

        public WineJuice getWineJuice() {
            return wineJuice;
        }
    }

    private WineJuice wineJuice;
    private FermentationActor(ActorContext<FermentationCommand> context, ActorRef<StorageActor.StorageCommand> sharedResource) {
        super(context);
        this.sharedResource = sharedResource;
        currentStorage = new StorageActor.Storage();
    }

    public static Behavior<FermentationCommand> behavior(ActorRef<StorageActor.StorageCommand> sharedResource) {
        return Behaviors.setup(context -> new FermentationActor(context, sharedResource));
    }

    private final ActorRef<StorageActor.StorageCommand> storage = getContext()
            .spawn(StorageActor.behavior(), "storageActor");

    @Override
    public Receive<FermentationCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(EmbossingProduct.class, this::onGetWineJuice)
                .onMessage(FermentationMessage.class, this::onCurrentStorage)
//                .onMessage(EmbossingProduct.class, this::onFerment)
                .build();
    }

    public static class FermentationMessage implements FermentationCommand {
        private String text;
        public StorageActor.Storage storage;

        public FermentationMessage(String text, StorageActor.Storage storage) {
            this.text = text;
            this.storage = storage;
        }
        public void setStorage(StorageActor.Storage storage) {
            this.storage = storage;
        }

    }

    private Behavior<FermentationCommand> onCurrentStorage(FermentationMessage msg) {
        System.out.println("fermentaion state" + msg.storage.toString());
        return this;
    }

    private Behavior<FermentationCommand> onFerment(){

        return this;
    }

    private Behavior<FermentationActor.FermentationCommand> onGetWineJuice(EmbossingProduct message){
        wineJuice = message.getWineJuice();
        System.out.println(message.getWineJuice().getLiters());
        storage.tell(new StorageActor.GetStateFermentation(getContext().getSelf()));
        return this;
    }

}
