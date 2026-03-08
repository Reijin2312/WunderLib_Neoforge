package de.ambertation.wunderlib.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import org.jetbrains.annotations.NotNull;

public abstract class NetworkPayload<T extends NetworkPayload<T>> implements CustomPacketPayload {
    public interface NetworkPayloadFactory<T extends NetworkPayload<T>> {
        T create(RegistryFriendlyByteBuf buf);
    }

    protected final PacketHandler<T> packetHandler;

    protected NetworkPayload(PacketHandler<T> packetHandler) {
        this.packetHandler = packetHandler;
    }

    protected abstract void write(RegistryFriendlyByteBuf buf);

    @Override
    public final @NotNull Type<T> type() {
        return this.packetHandler.CHANNEL;
    }

    public boolean isBlocking() {
        return false;
    }
}
