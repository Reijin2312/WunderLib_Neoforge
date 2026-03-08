package de.ambertation.wunderlib.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;


public abstract class ServerBoundNetworkPayload<T extends ServerBoundNetworkPayload<T>> extends NetworkPayload<T> {
    protected ServerBoundNetworkPayload(PacketHandler<T> packetHandler) {
        super(packetHandler);
    }

    protected abstract void prepareOnClient();

    protected abstract void processOnServer(ServerPlayer player, PacketSender responseSender);

    protected abstract void processOnGameThread(MinecraftServer server, ServerPlayer player);
}
