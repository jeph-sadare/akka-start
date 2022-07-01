package com.example;

public class AkkaStart {

    public static void main (String[] args) {
//        start actor system
        ActorSystem.create(IotSupervisor.create(), "iot-system");
    }
}
