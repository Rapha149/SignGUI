package de.rapha149.signgui;

import de.rapha149.signgui.version.VersionWrapper;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

public class SignGUI {

    static final VersionWrapper WRAPPER;
    static final String availableSignTypes;

    static {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
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
     * @return The new {@link de.rapha149.signgui.SignGUIBuilder} instance
     */
    public static SignGUIBuilder builder() {
        return new SignGUIBuilder();
    }

    private final String[] lines;
    private final Material type;
    private final DyeColor color;
    private final Location signLoc;
    private final SignGUIFinishHandler handler;
    private final boolean callHandlerSynchronously;
    private final JavaPlugin plugin;

    /**
     * Constructs a new SignGUI. Use {@link de.rapha149.signgui.SignGUI#builder()} to get a new instance.
     */
    SignGUI(String[] lines, Material type, DyeColor color, Location signLoc, SignGUIFinishHandler handler, boolean callHandlerSynchronously, JavaPlugin plugin) {
        this.lines = lines;
        this.type = type;
        this.color = color;
        this.signLoc = signLoc;
        this.handler = handler;
        this.callHandlerSynchronously = callHandlerSynchronously;
        this.plugin = plugin;
    }

    /**
     * Opens the sign gui for the player.
     *
     * @param player The player to open the gui for.
     * @throws de.rapha149.signgui.SignGUIException If an error occurs while opening the gui.
     */
    public void open(Player player) throws SignGUIException {
        // TODO check if player already has an open sign gui

        Validate.notNull(player, "The player cannot be null");

        try {
            WRAPPER.openSignEditor(player, lines, type, color, signLoc, (signEditor, resultLines) -> {
                Runnable runnable = () -> {
                    List<SignGUIAction> actions = handler.onFinish(player, new SignGUIResult(resultLines));
                    if (actions.stream().anyMatch(action -> action.execute(this, signEditor, player)))
                        WRAPPER.closeSignEditor(player, signEditor);
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
     * This is called when {@link de.rapha149.signgui.SignGUIAction#displayNewLines(String...)} is returned as an action after the player has finished editing.
     *
     * @param lines The lines, must be exactly 4.
     * @throws java.lang.IllegalArgumentException If lines is null or not exactly 4 lines.
     * @throws de.rapha149.signgui.SignGUIException If an error occurs while setting the lines.
     */
    void displayNewLines(Player player, SignEditor signEditor, String[] lines) {
        Validate.notNull(lines, "The lines cannot be null");
        Validate.isTrue(lines.length == 4, "The lines must have a length of 4");

        try {
            WRAPPER.displayNewLines(player, signEditor, lines);
        } catch (Exception e) {
            throw new SignGUIException("Failed to open sign gui", e);
        }
    }
}
