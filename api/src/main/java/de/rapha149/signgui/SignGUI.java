package de.rapha149.signgui;

import de.rapha149.signgui.SignGUIAction.SignGUIActionInfo;
import de.rapha149.signgui.version.VersionWrapper;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The base class of this api. Use {@link SignGUI#builder()} to get a new instance.
 */
public class SignGUI {

    static final VersionWrapper WRAPPER;
    static final String availableSignTypes;

    static {
        String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();

        String version = null;
        if (!craftBukkitPackage.contains(".v")) { // cb package not relocated (i.e. paper 1.20.5+)
            String bukkitVersion = Bukkit.getBukkitVersion();

            try {
                HttpURLConnection conn = (HttpURLConnection) new URL("https://raw.githubusercontent.com/Rapha149/NMSVersions/main/nms-versions.json").openConnection();
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
                    throw new IllegalStateException("SignGUI does not support bukkit server version \"" + bukkitVersion + "\"");
                }
            }
        } else {
            version = craftBukkitPackage.split("\\.")[3].substring(1);
        }

        try {
            WRAPPER = (VersionWrapper) Class.forName(VersionWrapper.class.getPackage().getName() + ".Wrapper" + version).getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException exception) {
            throw new IllegalStateException("Failed to load support for server version " + version, exception);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("SignGUI does not support the server version \"" + version + "\"", exception);
        }

        availableSignTypes = WRAPPER.getSignTypes().stream().map(Material::toString).collect(Collectors.joining(", "));
    }

    /**
     * Constructs a new SignGUIBuilder.
     *
     * @return The new {@link SignGUIBuilder} instance
     */
    public static SignGUIBuilder builder() {
        return new SignGUIBuilder();
    }

    private final String[] lines;
    private final Material type;
    private final DyeColor color;
    private final boolean glow;
    private final Location signLoc;
    private final SignGUIFinishHandler handler;
    private final boolean callHandlerSynchronously;
    private final JavaPlugin plugin;

    /**
     * Constructs a new SignGUI. Use {@link SignGUI#builder()} to get a new instance.
     */
    SignGUI(String[] lines, Material type, DyeColor color, boolean glow, Location signLoc, SignGUIFinishHandler handler, boolean callHandlerSynchronously, JavaPlugin plugin) {
        this.lines = lines;
        this.type = type;
        this.color = color;
        this.glow = glow;
        this.signLoc = signLoc;
        this.handler = handler;
        this.callHandlerSynchronously = callHandlerSynchronously;
        this.plugin = plugin;
    }

    /**
     * Opens the sign gui for the player.
     * <p>
     * Note: if there already is a sign gui open for the player, it will be closed and the {@link SignGUIFinishHandler} will not be called.
     * It is recommended to avoid opening a sign gui for a player that already has one open.
     *
     * @param player The player to open the gui for.
     * @throws SignGUIException If an error occurs while opening the gui.
     */
    public void open(Player player) throws SignGUIException {
        Validate.notNull(player, "The player cannot be null");

        try {
            WRAPPER.openSignEditor(player, lines, type, color, glow, signLoc, (signEditor, resultLines) -> {
                Runnable runnable = () -> {
                    Runnable close = () -> WRAPPER.closeSignEditor(player, signEditor);
                    List<SignGUIAction> actions = handler.onFinish(player, new SignGUIResult(resultLines));

                    if (actions == null || actions.isEmpty()) {
                        close.run();
                        return;
                    }

                    boolean keepOpen = false;
                    for (SignGUIAction action : actions) {
                        SignGUIActionInfo info = action.getInfo();
                        for (SignGUIAction otherAction : actions) {
                            if (action == otherAction)
                                continue;

                            SignGUIActionInfo otherInfo = otherAction.getInfo();
                            if (info.isConflicting(otherInfo)) {
                                close.run();
                                throw new SignGUIException("The actions " + info.getName() + " and " + otherInfo.getName() + " are conflicting");
                            }
                        }

                        if (info.isKeepOpen())
                            keepOpen = true;
                    }

                    if (!keepOpen)
                        close.run();
                    for (SignGUIAction action : actions)
                        action.execute(this, signEditor, player);
                };

                if (callHandlerSynchronously)
                    Bukkit.getScheduler().runTask(plugin, runnable);
                else
                    runnable.run();
            });
        } catch (Exception e) {
            throw new SignGUIException("Failed to open sign gui", e);
        }
    }

    /**
     * Sets the lines that are shown on the sign.
     * This is called when {@link SignGUIAction#displayNewLines(String...)} is returned as an action after the player has finished editing.
     *
     * @param lines The lines, must be exactly 4.
     * @throws java.lang.IllegalArgumentException If lines is null or not exactly 4 lines.
     * @throws SignGUIException                   If an error occurs while setting the lines.
     */
    void displayNewLines(Player player, SignEditor signEditor, String[] lines) {
        Validate.notNull(lines, "The lines cannot be null");
        Validate.isTrue(lines.length == 4, "The lines must have a length of 4");

        try {
            WRAPPER.displayNewLines(player, signEditor, lines);
        } catch (Exception e) {
            throw new SignGUIException("Failed to display new lines", e);
        }
    }
}
