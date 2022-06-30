package com.example;

import akka.actor.typed.ActorSystem;

public class AkkaStart {

    public static void main (String[] args) {
        ActorSystem.create(IotSupervisor.create(), "iot-system");
    }
}
