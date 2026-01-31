package de.ambertation.wunderlib.network;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.ApiStatus;

public class ServerBoundPacketHandler<T extends ServerBoundNetworkPayload<T>> extends PacketHandler<T> {
    private static final List<ServerBoundPacketHandler<?>> packetHandlers = new LinkedList<>();
    private static PayloadRegistrar registrar;
    private static SendToServerAdapter sendToServerAdapter;

    @ApiStatus.Internal
    static void registerAdapter(SendToServerAdapter adapter) {
        ServerBoundPacketHandler.sendToServerAdapter = adapter;
    }

    @ApiStatus.Internal
    public static void registerPayloads(PayloadRegistrar registrar) {
        ServerBoundPacketHandler.registrar = registrar;
        for (ServerBoundPacketHandler<?> packetHandler : packetHandlers) {
            registerPayload(registrar, packetHandler);
        }
        packetHandlers.clear();
    }

    public ServerBoundPacketHandler(
            Identifier channel,
            NetworkPayload.NetworkPayloadFactory<T> factory
    ) {
        super(channel, factory);
    }

    public static <T extends ServerBoundNetworkPayload<T>> void register(
            ServerBoundPacketHandler<T> packetHandler
    ) {
        if (registrar != null) {
            registerPayload(registrar, packetHandler);
        } else {
            packetHandlers.add(packetHandler);
        }
    }

    public static <T extends ServerBoundNetworkPayload<T>> ServerBoundPacketHandler<T> register(
            Identifier channel,
            NetworkPayload.NetworkPayloadFactory<T> factory
    ) {
        ServerBoundPacketHandler<T> packetHandler = new ServerBoundPacketHandler<>(channel, factory);
        register(packetHandler);
        return packetHandler;
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends ServerBoundNetworkPayload<T>> void sendToServer(T payload) {
        payload.prepareOnClient();
        if (sendToServerAdapter != null) {
            sendToServerAdapter.sendToServer(payload);
        } else {
            ClientPacketDistributor.sendToServer(payload);
        }
    }

    private static <T extends ServerBoundNetworkPayload<T>> void registerPayload(
            PayloadRegistrar registrar,
            ServerBoundPacketHandler<T> packetHandler
    ) {
        registrar.playToServer(packetHandler.CHANNEL, packetHandler.STREAM_CODEC, packetHandler::receiveOnServer);
    }

    private void receiveOnServer(
            T payload,
            IPayloadContext context
    ) {
        ServerPlayer player = (ServerPlayer) context.player();
        PacketSender responseSender = reply -> PacketDistributor.sendToPlayer(player, reply);
        payload.processOnServer(player, responseSender);

        CompletableFuture<Void> future = context.enqueueWork(() -> {
            var server = ((ServerLevel) player.level()).getServer();
            if (server != null) {
                payload.processOnGameThread(server, player);
            }
        });

        if (payload.isBlocking()) {
            future.join();
        }
    }
}

