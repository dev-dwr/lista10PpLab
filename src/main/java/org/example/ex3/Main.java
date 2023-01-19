package org.example.ex3;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import org.example.ex3.actors.EmbossingActor;
import org.example.ex3.actors.StorageActor;

public class Main {
    public static void main(String[] args) {
//        Water water = new Water(100);
//        Grapes grapes = new Grapes(50);
//        Suger suger = new Suger(30);
//        Bottels bottels = new Bottels(20);
//        StorageActor.Storage storage = new StorageActor.Storage(water, grapes, suger, bottels);

        ActorRef<StorageActor.StorageCommand> storageActor = ActorSystem.create(StorageActor.behavior(), "storageActor");
        ActorRef<EmbossingActor.EmbossingCommand> embossingActor = ActorSystem.create(EmbossingActor.behavior(storageActor), "embossingActor");

        EmbossingActor.EmbossingMessage msg = new EmbossingActor.EmbossingMessage("embossing");
        embossingActor.tell(msg);

    }
}
