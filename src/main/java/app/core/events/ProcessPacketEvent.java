package app.core.events;

import app.core.packetmodel.AppPacket;

public class ProcessPacketEvent {

    private AppPacket packet;

    public ProcessPacketEvent(AppPacket packet) {
        this.packet = packet;
    }

    public AppPacket getPacket() {
        return packet;
    }
}
