package org.example.sandbox;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;


public class Main {
    public static void main(String[] args) {
        ActorRef<StorageActor.Command> storageActor = ActorSystem.create(StorageActor.create(), "storageActor");
        ActorRef<EmbossingActor.Command> embossingActor = ActorSystem.create(EmbossingActor.create(), "embossingActor");

        embossingActor.tell(new EmbossingActor.StartEmbossingMsg("starting embosing ..."));
    }
}
