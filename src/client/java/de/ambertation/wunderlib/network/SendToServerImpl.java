package de.ambertation.wunderlib.network;

import net.neoforged.neoforge.network.PacketDistributor;

import org.jetbrains.annotations.ApiStatus;

public class SendToServerImpl implements SendToServerAdapter {
    @Override
    public void sendToServer(ServerBoundNetworkPayload<?> payload) {
        PacketDistributor.sendToServer(payload);
    }

    @ApiStatus.Internal
    public static void registerAdapter() {
        ServerBoundPacketHandler.registerAdapter(new SendToServerImpl());
    }
}

