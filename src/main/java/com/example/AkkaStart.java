package com.example;

import akka.actor.typed.ActorSystem;

public class AkkaStart {

    public static void main (String[] args) {
        System.out.println(" [>] IOT System has started");
        ActorSystem.create(IotSupervisor.create(), "iot-system");
    }
}
