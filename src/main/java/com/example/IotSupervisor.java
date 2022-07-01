package com.example;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class IotSupervisor extends AbstractBehavior<Void> {

//    main actor system
    private IotSupervisor(ActorContext<Void> context) {
        super(context);
        context.getLog().info(" [>] IoT application started");
    }

    public static Behavior<Void> create() {
        return Behaviors.setup(IotSupervisor::new);
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder()
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private IotSupervisor onPostStop() {
        getContext().getLog().info(" [!] IoT application stopped");
        return this;
    }
}
