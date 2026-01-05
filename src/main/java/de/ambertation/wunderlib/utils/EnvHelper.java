package de.ambertation.wunderlib.utils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Utility class to help determine the current environment type in a NeoForge mod.
 * This class provides methods to check whether code is running on the client or server side.
 */
public class EnvHelper {
    /**
     * Checks if the current environment is a client environment.
     * 
     * @return true if the current environment is a client, false if it's a server
     */
    public static boolean isClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }
}

