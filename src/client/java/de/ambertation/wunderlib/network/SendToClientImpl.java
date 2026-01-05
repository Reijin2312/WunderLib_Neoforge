package de.ambertation.wunderlib.network;

import org.jetbrains.annotations.ApiStatus;

public class SendToClientImpl implements SendToClientAdapter {
    @ApiStatus.Internal
    public static void registerAdapter() {
        ClientBoundPacketHandler.registerAdapter(new SendToClientImpl());
    }

    @Override
    public <T extends ClientBoundNetworkPayload<T>> void setupConnectionHandler(ClientBoundPacketHandler<T> packetHandler) {
        // NeoForge payloads are registered via RegisterPayloadHandlersEvent.
    }
}

