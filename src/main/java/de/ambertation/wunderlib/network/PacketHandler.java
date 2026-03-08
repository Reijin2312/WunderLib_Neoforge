package de.ambertation.wunderlib.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.NotNull;

public abstract class PacketHandler<T extends NetworkPayload<T>> {
    @NotNull
    public final CustomPacketPayload.Type<T> CHANNEL;

    @NotNull
    public final StreamCodec<RegistryFriendlyByteBuf, T> STREAM_CODEC;

    protected PacketHandler(
            @NotNull Identifier channel,
            @NotNull NetworkPayload.NetworkPayloadFactory<T> factory
    ) {
        this.CHANNEL = new CustomPacketPayload.Type<>(channel);

        this.STREAM_CODEC = CustomPacketPayload.codec(
                NetworkPayload::write,
                factory::create
        );
    }
}
