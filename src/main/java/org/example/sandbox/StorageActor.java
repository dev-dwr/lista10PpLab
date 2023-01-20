package org.example.sandbox;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class StorageActor extends AbstractBehavior<StorageActor.Command> {
    interface Command {
    }

    public static class GetState implements Command {
        private final ActorRef<StorageState> replyTo;

        public GetState(ActorRef<StorageState> replyTo) {
            this.replyTo = replyTo;
        }

        public ActorRef<StorageState> getReplyTo() {
            return replyTo;
        }
    }

    private StorageState state;

    public StorageActor(ActorContext<Command> context) {
        super(context);
        state = new StorageState(100, 100, 100, 100);
    }

    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetState.class, this::onGetState)
                .onMessage(RemoveGrapes.class, this::onRemoveGrapes)
                .build();
    }


    public static class RemoveGrapes implements Command {
        private final int amount;

        public RemoveGrapes(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static class RemoveWater implements Command {
        private final int amount;

        public RemoveWater(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static class RemoveSuger implements Command {
        private final int amount;

        public RemoveSuger(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(StorageActor::new);
    }

    private Behavior<Command> onRemoveGrapes(RemoveGrapes command) {
        state = new StorageState(state.getWaterLiters(), state.getGrapesKg() - 15, state.getSugarKg(), state.getBottles());
        return this;
    }

    private Behavior<Command> onGetState(GetState command) {
        command.getReplyTo().tell(state);
        return this;
    }

}
