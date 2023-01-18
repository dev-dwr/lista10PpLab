package org.example.ex3.actors;

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.example.ex3.model.Storage;

public class StorageActor extends AbstractBehavior<Storage> {
    private Storage storage;
    public StorageActor(ActorContext<Storage> context){
        super(context);
    }


    @Override
    public Receive<Storage> createReceive() {
        return newReceiveBuilder()
                .onMessage(Storage.class, this::testMethod)
                .build();
    }


    private Behavior<Storage> testMethod(Storage receivedStorage){
        System.out.println("Storage received");
        storage = receivedStorage;
        return this;
    }

    public static Behavior<Storage> behavior(){
        return Behaviors.setup(StorageActor::new);
    }
}
