package org.example.sandbox;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FermentationActor extends AbstractBehavior<FermentationActor.Command> {
    public interface Command {
    }

    public static class EmbossedWineJuice implements Command {
        private final int amount;

        public EmbossedWineJuice(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    @Override
    public Receive<FermentationActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(EmbossedWineJuice.class, this::fermentationStart)
                .build();
    }

    private Behavior<Command> fermentationStart(EmbossedWineJuice juice){
        System.out.println("juice" + juice.getAmount());
        return this;
    }
    public FermentationActor(ActorContext<FermentationActor.Command> context) {
        super(context);
    }

    public static Behavior<FermentationActor.Command> create() {
        return Behaviors.setup(FermentationActor::new);
    }

}
