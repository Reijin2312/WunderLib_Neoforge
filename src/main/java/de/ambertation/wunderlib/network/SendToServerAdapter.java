package de.ambertation.wunderlib.network;

public interface SendToServerAdapter {
    void sendToServer(ServerBoundNetworkPayload<?> payload);
}

