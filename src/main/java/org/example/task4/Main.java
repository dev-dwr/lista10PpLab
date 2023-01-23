package org.example.task4;

import akka.actor.typed.ActorSystem;

public class Main {
    public static void main(String[] args) {
        ActorSystem<StorageActor.Command> storageActor = ActorSystem.create(StorageActor.start(), "storageActor");
        storageActor.tell(StorageActor.Production.START);
    }
}
