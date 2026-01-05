package de.ambertation.wunderlib.utils;


import de.ambertation.wunderlib.WunderLib;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling semantic versioning in Minecraft mods.
 * This class provides methods for parsing, comparing, and converting version strings
 * in the format "major.minor.patch".
 */
public class Version {
    /** Constant representing version 0.0.0 */
    public static final Version ZERO = new Version(0, 0, 0);

    /**
     * Interface for mods that provide version information.
     * Implementing classes can provide mod version, ID, and utility methods for namespaces.
     */
    public interface ModVersionProvider {
        /**
         * Gets the version of the mod.
         * @return The version object
         */
        Version getModVersion();
        
        /**
         * Gets the ID of the mod.
         * @return The mod ID string
         */
        String getModID();

        /**
         * Gets the namespace for the mod, defaults to the mod ID.
         * @return The namespace string
         */
        default String getNamespace() {
            return getModID();
        }
        
        /**
         * Creates a ResourceLocation with the mod's namespace and the given path.
         * @param key The path part of the ResourceLocation
         * @return A ResourceLocation with the mod's namespace and the given path
         */
        default ResourceLocation mk(String key) {
            return ResourceLocation.fromNamespaceAndPath(getModID(), key);
        }
    }

    /** The version string in "major.minor.patch" format */
    public final String version;

    /**
     * Creates a new Version from a string.
     * If the input is "${version}" (a placeholder), it will default to "0.0.0".
     * 
     * @param version The version string in "major.minor.patch" format
     */
    public Version(String version) {
        if ("${version}".equals(version)) version = "0.0.0";
        this.version = version.trim();
    }

    /**
     * Creates a new Version from major, minor, and patch components.
     * 
     * @param major The major version number
     * @param minor The minor version number
     * @param patch The patch version number
     */
    public Version(int major, int minor, int patch) {
        this(String.format(Locale.ROOT, "%d.%d.%d", major, minor, patch));
    }

    /**
     * Converts a version string to an integer representation.
     * The version components are encoded into the RGB channels of a 32-bit integer.
     * 
     * @param version The version string to convert
     * @return An integer representation of the version
     */
    private static int toInt(String version) {
        if (version == null || version.isEmpty()) return 0;

        try {
            final String semanticVersionPattern = "(\\d+)\\.(\\d+)(\\.(\\d+))?\\D*";
            final Matcher matcher = Pattern.compile(semanticVersionPattern).matcher(version);

            int major = 0;
            int minor = 0;
            int patch = 0;
            if (matcher.find()) {
                if (matcher.groupCount() > 0)
                    major = matcher.group(1) == null ? 0 : Integer.parseInt(matcher.group(1));
                if (matcher.groupCount() > 1)
                    minor = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));
                if (matcher.groupCount() > 3)
                    patch = matcher.group(4) == null ? 0 : Integer.parseInt(matcher.group(4));
            }

            return ColorUtilARGB32.color(0, major, minor, patch);
        } catch (Exception e) {
            WunderLib.LOGGER.error("Failed to parse Version '" + version + "'.");
            return 0;
        }
    }

    /**
     * Extracts the major version component from an integer representation.
     * 
     * @param version The integer representation of the version
     * @return The major version number
     */
    public static int major(int version) {
        return ColorUtilARGB32.red(version);
    }

    /**
     * Extracts the minor version component from an integer representation.
     * 
     * @param version The integer representation of the version
     * @return The minor version number
     */
    public static int minor(int version) {
        return ColorUtilARGB32.green(version);
    }

    /**
     * Extracts the patch version component from an integer representation.
     * 
     * @param version The integer representation of the version
     * @return The patch version number
     */
    public static int patch(int version) {
        return ColorUtilARGB32.blue(version);
    }

    /**
     * Creates a Version object from an integer representation.
     * 
     * @param version The integer representation of the version
     * @return A Version object
     */
    public static Version fromInt(int version) {
        return new Version(major(version), minor(version), patch(version));
    }

    /**
     * Converts this Version object to an integer representation.
     * 
     * @return An integer representation of this version
     */
    public int toInt() {
        return toInt(version);
    }

    /**
     * Checks if this version is larger than the given version.
     * 
     * @param v2 The version to compare against
     * @return True if this version is larger, false otherwise
     */
    public boolean isLargerThan(Version v2) {
        return toInt() > v2.toInt();
    }

    /**
     * Checks if this version is larger than or equal to the given version.
     * 
     * @param v2 The version to compare against
     * @return True if this version is larger or equal, false otherwise
     */
    public boolean isLargerOrEqualVersion(Version v2) {
        return toInt() >= v2.toInt();
    }

    /**
     * Checks if this version is larger than the given version string.
     * 
     * @param v2 The version string to compare against
     * @return True if this version is larger, false otherwise
     */
    public boolean isLargerThan(String v2) {
        return toInt() > toInt(v2);
    }

    /**
     * Checks if this version is larger than or equal to the given version string.
     * 
     * @param v2 The version string to compare against
     * @return True if this version is larger or equal, false otherwise
     */
    public boolean isLargerOrEqualVersion(String v2) {
        return toInt() >= toInt(v2);
    }

    /**
     * Checks if this version is less than the given version.
     * 
     * @param v2 The version to compare against
     * @return True if this version is less, false otherwise
     */
    public boolean isLessThan(Version v2) {
        return toInt() < v2.toInt();
    }

    /**
     * Checks if this version is less than or equal to the given version.
     * 
     * @param v2 The version to compare against
     * @return True if this version is less or equal, false otherwise
     */
    public boolean isLessOrEqualVersion(Version v2) {
        return toInt() <= v2.toInt();
    }

    /**
     * Checks if this version is less than the given version string.
     * 
     * @param v2 The version string to compare against
     * @return True if this version is less, false otherwise
     */
    public boolean isLessThan(String v2) {
        return toInt() < toInt(v2);
    }

    /**
     * Checks if this version is less than or equal to the given version string.
     * 
     * @param v2 The version string to compare against
     * @return True if this version is less or equal, false otherwise
     */
    public boolean isLessOrEqualVersion(String v2) {
        return toInt() <= toInt(v2);
    }

    /**
     * Checks if this version is equal to another object.
     * If the object is a string, it compares the trimmed version string.
     * If the object is a Version, it compares the version strings.
     * 
     * @param o The object to compare against
     * @return True if the versions are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof String s) return version.equals(s.trim());
        if (!(o instanceof Version version1)) return false;
        return Objects.equals(version, version1.version);
    }

    /**
     * Computes the hash code for this version.
     * 
     * @return The hash code of the version string
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(version);
    }

    /**
     * Returns the string representation of this version.
     * 
     * @return The version string
     */
    @Override
    public String toString() {
        return version;
    }
}

