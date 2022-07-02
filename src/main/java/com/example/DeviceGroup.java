package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;
import java.util.Map;

public class DeviceGroup extends AbstractBehavior<DeviceGroup.Command> {

    public interface Command {}

    private class DeviceTerminated implements Command {
        public final ActorRef<Device.Command> device;
        public final String groupId;
        public final String deviceId;

        DeviceTerminated(ActorRef<Device.Command> device, String groupId, String deviceId) {
            this.device = device;
            this.groupId = groupId;
            this.deviceId = deviceId;
        }
    }

    private final String groupId;
    private final Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();

    private DeviceGroup(ActorContext<Command> context, String groupId) {
        super(context);
        this.groupId = groupId;
        context.getLog().info(" [>] DeviceGroup {} started", groupId);
    }

    public static Behavior<Command> create(String groupId) {
        return Behaviors.setup(context -> new DeviceGroup(context, groupId));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(DeviceManager.RequestTrackDevice.class, this::onTrackDevice)
                .onMessage(DeviceManager.RequestDeviceList.class, request -> request.groupId.equalsIgnoreCase(groupId), this::onDeviceList)
                .onMessage(DeviceTerminated.class, this::onTerminated)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private DeviceGroup onDeviceList(DeviceManager.RequestDeviceList request) {
        request.replyTo.tell(new DeviceManager.ReplyDeviceList(request.requestId, deviceIdToActor.keySet()));
        return this;
    }

    private DeviceGroup onTerminated(DeviceTerminated terminated) {
        getContext().getLog().info(" [!] Device actor for {} has been terminated", terminated.deviceId);
        deviceIdToActor.remove(terminated.deviceId);
        return this;
    }

    private DeviceGroup onPostStop() {
        getContext().getLog().info(" [!] DeviceGroup {} stopped", groupId);
        return this;
    }

    private DeviceGroup onTrackDevice(DeviceManager.RequestTrackDevice trackMsg) {
        if (this.groupId.equalsIgnoreCase(trackMsg.groupId)) {
            ActorRef<Device.Command> deviceActor = deviceIdToActor.get(trackMsg.deviceId);
            if (deviceActor != null) {
                trackMsg.replyTo.tell(new DeviceManager.DeviceRegistered(deviceActor));
            } else {
                getContext().getLog().info(" [>] Creating device actor for {}", trackMsg.deviceId);
                deviceActor = getContext().spawn(Device.create(groupId, trackMsg.deviceId), "device-" + trackMsg.deviceId);
                getContext().watchWith(deviceActor, new DeviceTerminated(deviceActor, groupId, trackMsg.deviceId));
                deviceIdToActor.put(trackMsg.deviceId, deviceActor);
                trackMsg.replyTo.tell(new DeviceManager.DeviceRegistered(deviceActor));
            }
        } else {
            getContext().getLog().warn(" [!] Ignoring TrackDevice request for {}. This actor is responsible for {}", groupId, this.groupId);
        }
        return this;
    }
}
