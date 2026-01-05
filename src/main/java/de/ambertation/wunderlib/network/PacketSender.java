package de.ambertation.wunderlib.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@FunctionalInterface
public interface PacketSender {
    void send(CustomPacketPayload payload);
}
