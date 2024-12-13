package de.rapha149.signgui.version;

import de.rapha149.signgui.exception.SignGUIVersionException;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * A utility class to get the version wrapper for the server version.
 */
public class VersionMatcher {

    private static boolean initialized;
    private static VersionWrapper wrapper;

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

        String version = null;
        if (!craftBukkitPackage.contains(".v")) { // cb package not relocated (i.e. paper 1.20.5+)
            String bukkitVersion = Bukkit.getBukkitVersion();

            try {
                HttpURLConnection conn = (HttpURLConnection) new URL("https://raw.githubusercontent.com/Rapha149/NMSVersions/main/nms-versions.json").openConnection();
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.connect();

                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    JSONObject json = (JSONObject) new JSONParser().parse(br.lines().collect(Collectors.joining()));
                    if (json.containsKey(bukkitVersion))
                        version = (String) json.get(bukkitVersion);
                }
            } catch (IOException | ParseException e) {
                Bukkit.getLogger().warning("[SignGUI] Can't access online NMS versions list, falling back to hardcoded NMS versions. These could be outdated.");
            }

            if (version == null) {
                // separating major and minor versions, example: 1.20.4-R0.1-SNAPSHOT -> major = 20, minor = 4
                final String[] versionNumbers = bukkitVersion.split("-")[0].split("\\.");
                int major = Integer.parseInt(versionNumbers[1]);
                int minor = versionNumbers.length > 2 ? Integer.parseInt(versionNumbers[2]) : 0;

                if (major == 20 && minor >= 5) { // 1.20.5, 1.20.6
                    version = "1_20_R4";
                } else if (major == 21 && minor == 0) { // 1.21
                    version = "1_21_R1";
                } else {
                    throw new SignGUIVersionException("SignGUI does not support bukkit server version \"" + bukkitVersion + "\"");
                }
            }
        } else {
            version = craftBukkitPackage.split("\\.")[3].substring(1);
        }

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
