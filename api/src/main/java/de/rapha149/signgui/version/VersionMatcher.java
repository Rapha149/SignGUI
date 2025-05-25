package de.rapha149.signgui.version;

import de.rapha149.signgui.exception.SignGUIVersionException;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class to get the version wrapper for the server version.
 */
public class VersionMatcher {

    private static final Map<String, String> VERSIONS;
    private static final String NEWEST_VERSION = "1_21_R4";

    private static boolean initialized;
    private static VersionWrapper wrapper;

    static {
        Map<String, String> versions = new HashMap<>();
        versions.put("1.20.5", "1_20_R4");
        versions.put("1.20.6", "1_20_R4");
        versions.put("1.21.1", "1_21_R1");
        versions.put("1.21.3", "1_21_R2");
        versions.put("1.21.4", "1_21_R3");
        versions.put("1.21.5", "1_21_R4");
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

    /**
     * Internal method to initialize the version wrapper.
     */
    private static VersionWrapper initWrapper() throws SignGUIVersionException {
        String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
        String version = craftBukkitPackage.contains(".v") ? craftBukkitPackage.split("\\.")[3].substring(1) :
                VERSIONS.getOrDefault(Bukkit.getBukkitVersion().split("-")[0], NEWEST_VERSION);

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

    private static boolean useMojangMappings(String version) {
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
        } catch (ClassNotFoundException ignored) {
            return false;
        }

        final String[] versionNumbers = version.replace("R", "").split("_");
        int major = Integer.parseInt(versionNumbers[1]);
        int minor = versionNumbers.length > 2 ? Integer.parseInt(versionNumbers[2]) : 0;
        if (major == 20 && minor == 4)
            return true; // 1.20.5/6
        return major > 20; // >= 1.21
    }
}
