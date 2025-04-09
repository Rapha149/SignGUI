package de.rapha149.signgui.version;

import de.rapha149.signgui.SignGUI;

import de.rapha149.signgui.exception.SignGUIVersionException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;
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
    public static void getWrapperAsync(Consumer<VersionWrapper> callback, Consumer<Exception> errorCallback) {
        if (!initialized) {
            initialized = true;
            initWrapperAsync(wrapper -> {
                if (wrapper == null) {
                    errorCallback.accept(new SignGUIVersionException("Initialization failed. " +
                            "This could be because this server version is not supported or due to an initialization error."));
                } else {
                    // Save the successfully loaded wrapper for later synchronous access if needed.
                    VersionMatcher.wrapper = wrapper;
                    callback.accept(wrapper);
                }
            });
        } else if (VersionMatcher.wrapper == null) {
            errorCallback.accept(new SignGUIVersionException("The previous attempt to initialize the version wrapper failed. " +
                    "This could be because this server version is not supported or because an error occurred during initialization."));
        } else {
            callback.accept(VersionMatcher.wrapper);
        }
    }

    /**
     * Internal method to initialize the version wrapper.
     */
    public static void initWrapperAsync(Consumer<VersionWrapper> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(SignGUI.plugin, () -> {
            String version = null;
            String bukkitVersion = Bukkit.getBukkitVersion();

            String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
            if (!craftBukkitPackage.contains(".v")) { // cb package not relocated (e.g. paper 1.20.5+)
                try {
                    URL url = new URL("https://raw.githubusercontent.com/Rapha149/NMSVersions/main/nms-versions.json");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String jsonData = br.lines().collect(Collectors.joining());
                        JSONObject json = (JSONObject) new JSONParser().parse(jsonData);
                        if (json.containsKey(bukkitVersion)) {
                            version = (String) json.get(bukkitVersion);
                        }
                    }
                } catch (IOException | ParseException e) {
                    Bukkit.getLogger().warning("[SignGUI] Can't access online NMS versions list, falling back to hardcoded NMS versions. These could be outdated.");
                }

                // separating major and minor versions, example: 1.20.4-R0.1-SNAPSHOT -> major = 20, minor = 4
                if (version == null) {
                    final String[] versionNumbers = bukkitVersion.split("-")[0].split("\\.");
                    int major = Integer.parseInt(versionNumbers[1]);
                    int minor = versionNumbers.length > 2 ? Integer.parseInt(versionNumbers[2]) : 0;
                    if (major == 20 && minor >= 5) { // 1.20.5, 1.20.6
                        version = "1_20_R4";
                    } else if (major == 21 && minor == 0) { // 1.21
                        version = "1_21_R1";
                    } else {
                        // In the asynchronous branch, you may opt to call the callback with null or handle error appropriately
                        final String errorMsg = "SignGUI does not support bukkit server version \"" + bukkitVersion + "\"";
                        Bukkit.getScheduler().runTask(SignGUI.plugin, () -> {
                            callback.accept(null); // or alternatively, throw an exception in a controlled way
                            Bukkit.getLogger().severe(errorMsg);
                        });
                        return;
                    }
                }
            } else {
                version = craftBukkitPackage.split("\\.")[3].substring(1);
            }

            // Determine proper class name based on mapping
            final String className;
            if (useMojangMappings(version)) {
                className = VersionWrapper.class.getPackage().getName() + ".MojangWrapper" + version;
            } else {
                className = VersionWrapper.class.getPackage().getName() + ".Wrapper" + version;
            }

            // Load and instantiate the class (Reflection must be done on main thread if it interacts with server internals)
            String finalVersion = version;
            Bukkit.getScheduler().runTask(SignGUI.plugin, () -> {
                try {
                    VersionWrapper wrapper = (VersionWrapper) Class.forName(className).getDeclaredConstructor().newInstance();
                    callback.accept(wrapper);
                } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException exception) {
                    Bukkit.getLogger().severe("Failed to load support for server version " + finalVersion);
                    callback.accept(null);
                } catch (ClassNotFoundException exception) {
                    Bukkit.getLogger().severe("SignGUI does not support the server version \"" + finalVersion + "\"");
                    callback.accept(null);
                }
            });
        });
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
