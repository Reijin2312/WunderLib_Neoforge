package de.ambertation.wunderlib.network;

public interface SendToClientAdapter {
    <T extends ClientBoundNetworkPayload<T>> void setupConnectionHandler(ClientBoundPacketHandler<T> packetHandler);
}

