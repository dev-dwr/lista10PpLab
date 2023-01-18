package org.example.ex3;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import org.example.ex3.actors.StorageActor;
import org.example.ex3.model.Storage;
import org.example.ex3.products.Bottels;
import org.example.ex3.products.Grapes;
import org.example.ex3.products.Suger;
import org.example.ex3.products.Water;

public class Main {
    public static void main(String[] args) {
        Water water = new Water(100);
        Grapes grapes = new Grapes(50);
        Suger suger = new Suger(30);
        Bottels bottels = new Bottels(20);
        Storage storage = new Storage(water, grapes, suger, bottels);

        ActorRef<Storage> storageActor = ActorSystem.create(StorageActor.behavior(), "storageActor");
        storageActor.tell(storage);

    }
}
