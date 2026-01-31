package de.ambertation.wunderlib.network;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.ApiStatus;

public class ClientBoundPacketHandler<T extends ClientBoundNetworkPayload<T>> extends PacketHandler<T> {
    private static final List<ClientBoundPacketHandler<?>> packetHandlers = new LinkedList<>();
    private static PayloadRegistrar registrar;
    private static SendToClientAdapter sendToClientAdapter;

    @ApiStatus.Internal
    static void registerAdapter(SendToClientAdapter adapter) {
        ClientBoundPacketHandler.sendToClientAdapter = adapter;
        for (ClientBoundPacketHandler<?> packetHandler : packetHandlers) {
            adapter.setupConnectionHandler(packetHandler);
        }
    }

    @ApiStatus.Internal
    public static void registerPayloads(PayloadRegistrar registrar) {
        ClientBoundPacketHandler.registrar = registrar;
        for (ClientBoundPacketHandler<?> packetHandler : packetHandlers) {
            registerPayload(registrar, packetHandler);
        }
        packetHandlers.clear();
    }

    public ClientBoundPacketHandler(
            Identifier channel,
            NetworkPayload.NetworkPayloadFactory<T> factory
    ) {
        super(channel, factory);
    }

    public static <T extends ClientBoundNetworkPayload<T>> void register(
            ClientBoundPacketHandler<T> packetHandler
    ) {
        if (registrar != null) {
            registerPayload(registrar, packetHandler);
        } else {
            packetHandlers.add(packetHandler);
        }
        if (sendToClientAdapter != null) {
            sendToClientAdapter.setupConnectionHandler(packetHandler);
        }
    }

    public static <T extends ClientBoundNetworkPayload<T>> ClientBoundPacketHandler<T> register(
            Identifier channel,
            NetworkPayload.NetworkPayloadFactory<T> factory
    ) {
        ClientBoundPacketHandler<T> packetHandler = new ClientBoundPacketHandler<>(channel, factory);
        register(packetHandler);
        return packetHandler;
    }

    public static <T extends ClientBoundNetworkPayload<T>> void sendToClient(ServerPlayer player, T payload) {
        //when starting a game in the UI, the environment is always client
//        if (!EnvHelper.isClient()) {
        payload.prepareOnServer(player);
        PacketDistributor.sendToPlayer(player, payload);
//        } else {
//            //
//        }
    }

    public static <T extends ClientBoundNetworkPayload<T>> void sendToClient(
            ServerLevel serverLevel,
            T payload
    ) {
//        if (!EnvHelper.isClient()) {
        sendToClient(serverLevel.players(), payload);
//        } else {
//            //
//        }
    }

    public static <T extends ClientBoundNetworkPayload<T>> void sendToClient(
            Collection<ServerPlayer> players,
            T payload
    ) {
//        if (!EnvHelper.isClient()) {
        players.forEach(player -> {
            payload.prepareOnServer(player);
            PacketDistributor.sendToPlayer(player, payload);
        });
//        } else {
//            //
//        }
    }

    private static <T extends ClientBoundNetworkPayload<T>> void registerPayload(
            PayloadRegistrar registrar,
            ClientBoundPacketHandler<T> packetHandler
    ) {
        registrar.playToClient(packetHandler.CHANNEL, packetHandler.STREAM_CODEC, packetHandler::receiveOnClient);
    }

    private void receiveOnClient(
            T payload,
            IPayloadContext context
    ) {
        payload.processOnClient(reply -> ClientPacketDistributor.sendToServer(reply));

        CompletableFuture<Void> future = context.enqueueWork(() -> {
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
                payload.processOnGameThread(client);
            }
        });

        if (payload.isBlocking()) {
            future.join();
        }
    }
}
