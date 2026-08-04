package de.rapha149.signgui.version;

import de.rapha149.signgui.exception.SignGUIVersionException;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class to get the version wrapper for the server version.
 * Prefers Minecraft protocol version (PacketUxUi-style), with CraftBukkit package
 * and Bukkit version fallbacks for older / unversioned servers.
 */
public class VersionMatcher {

    private static final Map<String, String> VERSIONS;
    private static final String NEWEST_VERSION = "26_2";

    private static boolean initialized;
    private static VersionWrapper wrapper;

    static {
        Map<String, String> versions = new HashMap<>();
        versions.put("1.8", "1_8_R1");
        versions.put("1.8.3", "1_8_R2");
        versions.put("1.8.4", "1_8_R3");
        versions.put("1.8.5", "1_8_R3");
        versions.put("1.8.6", "1_8_R3");
        versions.put("1.8.7", "1_8_R3");
        versions.put("1.8.8", "1_8_R3");
        versions.put("1.8.9", "1_8_R3");
        versions.put("1.9", "1_9_R1");
        versions.put("1.9.2", "1_9_R1");
        versions.put("1.9.4", "1_9_R2");
        versions.put("1.10.2", "1_10_R1");
        versions.put("1.11", "1_11_R1");
        versions.put("1.11.2", "1_11_R1");
        versions.put("1.12", "1_12_R1");
        versions.put("1.12.2", "1_12_R1");
        versions.put("1.13", "1_13_R1");
        versions.put("1.13.1", "1_13_R1");
        versions.put("1.13.2", "1_13_R2");
        versions.put("1.14", "1_14_R1");
        versions.put("1.14.4", "1_14_R1");
        versions.put("1.15", "1_15_R1");
        versions.put("1.15.2", "1_15_R1");
        versions.put("1.16.1", "1_16_R1");
        versions.put("1.16.2", "1_16_R2");
        versions.put("1.16.3", "1_16_R2");
        versions.put("1.16.4", "1_16_R3");
        versions.put("1.16.5", "1_16_R3");
        versions.put("1.17", "1_17_R1");
        versions.put("1.17.1", "1_17_R1");
        versions.put("1.18", "1_18_R1");
        versions.put("1.18.1", "1_18_R1");
        versions.put("1.18.2", "1_18_R2");
        versions.put("1.19", "1_19_R1");
        versions.put("1.19.1", "1_19_R1");
        versions.put("1.19.2", "1_19_R1");
        versions.put("1.19.3", "1_19_R2");
        versions.put("1.19.4", "1_19_R3");
        versions.put("1.20", "1_20_R1");
        versions.put("1.20.1", "1_20_R1");
        versions.put("1.20.2", "1_20_R2");
        versions.put("1.20.3", "1_20_R3");
        versions.put("1.20.4", "1_20_R3");
        versions.put("1.20.5", "1_20_R4");
        versions.put("1.20.6", "1_20_R4");
        versions.put("1.21", "1_21_R1");
        versions.put("1.21.1", "1_21_R1");
        versions.put("1.21.2", "1_21_R2");
        versions.put("1.21.3", "1_21_R2");
        versions.put("1.21.4", "1_21_R3");
        versions.put("1.21.5", "1_21_R4");
        versions.put("1.21.6", "1_21_R5");
        versions.put("1.21.7", "1_21_R5");
        versions.put("1.21.8", "1_21_R5");
        versions.put("1.21.9", "1_21_R6");
        versions.put("1.21.10", "1_21_R6");
        versions.put("1.21.11", "1_21_R7");
        // Year-based versions: CraftBukkit packages are unversioned.
        versions.put("26.1", "26_1");
        versions.put("26.1.1", "26_1");
        versions.put("26.1.2", "26_1");
        versions.put("26.2", "26_2");
        VERSIONS = Collections.unmodifiableMap(versions);
    }

    /**
     * Returns the appropriate version wrapper for the current server version.
     * If this method is called for the first time, the wrapper is initialized beforehand.
     *
     * @return The {@link VersionWrapper} instance
     * @throws SignGUIVersionException If the server version is not supported by this api or an error occured during initialization.
     */
    public static VersionWrapper getWrapper() throws SignGUIVersionException {
        if (!initialized) {
            initialized = true;
            return wrapper = initWrapper();
        } else if (wrapper == null) {
            throw new SignGUIVersionException("The previous attempt to initialize the version wrapper failed. " +
                                              "This could be because this server version is not supported or " +
                                              "because an error occured during initialization.");
        } else {
            return wrapper;
        }
    }

    private static VersionWrapper initWrapper() throws SignGUIVersionException {
        String version = resolveBucket();

        final String className;
        if (useMojangMappings(version)) {
            className = VersionWrapper.class.getPackage().getName() + ".MojangWrapper" + version;
        } else {
            className = VersionWrapper.class.getPackage().getName() + ".Wrapper" + version;
        }

        try {
            return (VersionWrapper) Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException exception) {
            throw new SignGUIVersionException("Failed to load support for server version " + version, exception);
        } catch (ClassNotFoundException exception) {
            throw new SignGUIVersionException("SignGUI does not support the server version \"" + version + "\"", exception);
        }
    }

    /**
     * Resolves the NMS bucket id for the running server.
     */
    static String resolveBucket() throws SignGUIVersionException {
        String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
        if (craftBukkitPackage.contains(".v")) {
            String[] parts = craftBukkitPackage.split("\\.");
            if (parts.length >= 4 && parts[3].startsWith("v")) {
                return parts[3].substring(1);
            }
        }

        try {
            int protocol = ProtocolProbe.current();
            String byProtocol = ProtocolVersions.bucketForProtocol(protocol);
            if (byProtocol != null) {
                return byProtocol;
            }
        } catch (SignGUIVersionException ignored) {
            // fall through to Bukkit version map
        }

        String minecraftVersion = normalizeMinecraftVersion(Bukkit.getBukkitVersion());
        String mapped = VERSIONS.get(minecraftVersion);
        if (mapped != null) {
            return mapped;
        }

        int lastDot = minecraftVersion.lastIndexOf('.');
        int firstDot = minecraftVersion.indexOf('.');
        if (lastDot > firstDot) {
            mapped = VERSIONS.get(minecraftVersion.substring(0, lastDot));
            if (mapped != null) {
                return mapped;
            }
        }

        return VERSIONS.getOrDefault(minecraftVersion, NEWEST_VERSION);
    }

    static String normalizeMinecraftVersion(String bukkitVersion) {
        String version = bukkitVersion;
        int dash = version.indexOf('-');
        if (dash >= 0) {
            version = version.substring(0, dash);
        }
        int buildIdx = version.indexOf(".build.");
        if (buildIdx >= 0) {
            version = version.substring(0, buildIdx);
        }
        return version;
    }

    private static boolean useMojangMappings(String version) {
        // Year-based buckets (26.x+): Spigot and Paper both ship Mojang-mapped servers.
        if (!version.startsWith("1_")) {
            return true;
        }

        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
        } catch (ClassNotFoundException ignored) {
            return false;
        }

        final String[] versionNumbers = version.replace("R", "").split("_");
        int major = Integer.parseInt(versionNumbers[1]);
        int minor = versionNumbers.length > 2 ? Integer.parseInt(versionNumbers[2]) : 0;
        if (major == 20 && minor == 4) {
            return true; // 1.20.5/6
        }
        return major > 20; // >= 1.21
    }
}
