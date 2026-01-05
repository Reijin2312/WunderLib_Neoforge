package de.ambertation.wunderlib.network;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public abstract class ClientBoundNetworkPayload<T extends ClientBoundNetworkPayload<T>> extends NetworkPayload<T> {
    protected ClientBoundNetworkPayload(PacketHandler<T> packetHandler) {
        super(packetHandler);
    }

    protected abstract void prepareOnServer(ServerPlayer player);

    @OnlyIn(Dist.CLIENT)
    protected abstract void processOnClient(PacketSender responseSender);

    @OnlyIn(Dist.CLIENT)
    protected abstract void processOnGameThread(Minecraft client);
}

