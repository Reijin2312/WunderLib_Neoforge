package de.ambertation.wunderlib;

import de.ambertation.wunderlib.general.Logger;
import de.ambertation.wunderlib.network.ClientBoundPacketHandler;
import de.ambertation.wunderlib.network.ServerBoundPacketHandler;

import net.minecraft.resources.Identifier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(WunderLib.MOD_ID)
public class WunderLib {
    public static final String MOD_ID = "wunderlib";
    public static final Logger LOGGER = new Logger();

    public WunderLib(IEventBus modBus) {
        modBus.addListener(WunderLib::registerPayloadHandlers);
    }

    public static Identifier ID(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(MOD_ID);
        ClientBoundPacketHandler.registerPayloads(registrar);
        ServerBoundPacketHandler.registerPayloads(registrar);
    }
}
