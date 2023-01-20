package org.example.sandbox;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class EmbossingActor extends AbstractBehavior<EmbossingActor.Command> {
    interface Command {}


    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartEmbossingMsg.class, this::startEmbossing)
                .build();
    }

    public EmbossingActor(ActorContext<Command> context) {
        super(context);
    }

    public static class StartEmbossingMsg implements Command{
        private final String text;

        public StartEmbossingMsg(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }


    private final ActorRef<StorageActor.Command> storage = getContext()
            .spawn(StorageActor.create(), "storageActor");

    private final ActorRef<FermentationActor.Command> fermentation = getContext()
            .spawn(FermentationActor.create(), "fermentationActor");

    private Behavior<Command> getStateFromStorage(StartEmbossingMsg msg){
//        storage.tell(new StorageActor.GetState());
        return this;
    }
    private Behavior<Command> startEmbossing(StartEmbossingMsg msg){
        //remove grapes
        storage.tell(new StorageActor.RemoveGrapes(15));

        fermentation.tell(new FermentationActor.EmbossedWineJuice(10));
        return this;
    }

    public static Behavior<EmbossingActor.Command> create() {
        return Behaviors.setup(EmbossingActor::new);
    }

}
