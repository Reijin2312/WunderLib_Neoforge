package de.ambertation.wunderlib.network;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;


public abstract class ClientBoundNetworkPayload<T extends ClientBoundNetworkPayload<T>> extends NetworkPayload<T> {
    protected ClientBoundNetworkPayload(PacketHandler<T> packetHandler) {
        super(packetHandler);
    }

    protected abstract void prepareOnServer(ServerPlayer player);

    protected abstract void processOnClient(PacketSender responseSender);

    protected abstract void processOnGameThread(Minecraft client);
}
