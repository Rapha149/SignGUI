package de.rapha149.signgui;

import de.rapha149.signgui.version.VersionWrapper;
import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Builder for {@link SignGUI}.
 */
public class SignGUIBuilder {

    private static String availableSignTypes;

    private static String getAvailableSignTypes(VersionWrapper wrapper) {
        if (availableSignTypes == null)
            availableSignTypes = wrapper.getSignTypes().stream().map(Material::toString).collect(Collectors.joining(", "));
        return availableSignTypes;
    }

    private VersionWrapper wrapper;
    private String[] lines = new String[4];
    private Object[] adventureLines = null;
    private Material type;
    private DyeColor color = DyeColor.BLACK;
    private boolean glow = false;
    private Location loc;
    private SignGUIFinishHandler handler;
    private boolean callHandlerSynchronously = false;
    private JavaPlugin plugin;

    /**
     * Constructs a new SignGUIBuilder. Use {@link SignGUI#builder()} to get a new instance.
     */
    SignGUIBuilder(VersionWrapper wrapper) {
        this.wrapper = wrapper;
        this.type = wrapper.getDefaultType();
    }

    /**
     * Sets the lines that are shown on the sign.
     *
     * @param lines The lines, may be less than 4.
     * @return The {@link SignGUIBuilder} instance
     * @throws java.lang.IllegalArgumentException If lines is null.
     */
    public SignGUIBuilder setLines(String... lines) {
        Validate.notNull(lines, "The lines cannot be null");
        this.lines = Arrays.copyOf(lines, 4);
        return this;
    }

    /**
     * Sets a specific line that is shown on the sign.
     *
     * @param index The index of the line.
     * @param line  The line.
     * @return The {@link SignGUIBuilder} instance
     * @throws java.lang.IllegalArgumentException If the index is below 0 or above 4.
     */
    public SignGUIBuilder setLine(int index, String line) {
        Validate.isTrue(index >= 0 && index <= 3, "Index out of range");
        lines[index] = line;
        return this;
    }

    /**
     * Sets the lines that are shown on the sign using an Adventure component (1.20.5+)
     * Lines set using this method are only shown when using a mojang-mapped Paper plugin.
     * It is recommended to also set fallback lines using {@link #setLines(String...)} as these will be used if the Adventure components cannot be used for some reason.
     *
     * @param adventureLines The lines, may be less than 4.
     * @return The {@link SignGUIBuilder} instance
     * @throws java.lang.IllegalArgumentException If lines is null.
     */
    public SignGUIBuilder setAdventureLines(Object... adventureLines) {
        Validate.notNull(adventureLines, "The adventure lines cannot be null");
        this.adventureLines = Arrays.copyOf(adventureLines, 4);
        return this;
    }

    /**
     * Sets a specific line that is shown on the sign using an Adventure component (1.20.5+)
     * Lines set using this method are only shown when using a mojang-mapped Paper plugin.
     * It is recommended to also set fallback lines using {@link #setLine(int, String)} as these will be used if the Adventure components cannot be used for some reason.
     *
     * @param index The index of the line.
     * @param component  Adventure component
     * @return The {@link SignGUIBuilder} instance
     * @throws java.lang.IllegalArgumentException If the index is below 0 or above 4.
     */
    public SignGUIBuilder setAdventureLine(int index, Object component) {
        Validate.isTrue(index >= 0 && index <= 3, "Index out of range");
        if (adventureLines == null)
            adventureLines = new Object[4];
        adventureLines[index] = component;
        return this;
    }

    /**
     * Sets the type of the sign.
     *
     * @param type The type. Must be a sign type.
     * @return The {@link SignGUIBuilder} instance
     * @throws java.lang.IllegalArgumentException If type is null or not a sign type.
     */
    public SignGUIBuilder setType(Material type) {
        Validate.notNull(type, "The type cannot be null");
        Validate.isTrue(wrapper.getSignTypes().contains(type), type + " is not a sign type. Available sign types: " + getAvailableSignTypes(wrapper));
        this.type = type;
        return this;
    }

    /**
     * Sets the color of the sign. (1.14+)
     *
     * @param color The color.
     * @return The {@link SignGUIBuilder} instance
     * @throws java.lang.IllegalArgumentException If color is null.
     */
    public SignGUIBuilder setColor(DyeColor color) {
        Validate.notNull(color, "The color cannot be null");
        this.color = color;
        return this;
    }

    /**
     * Sets if the sign's text should glow. (1.17+)
     *
     * @param glow If the sign's text should glow.
     * @return The {@link SignGUIBuilder} instance
     */
    public SignGUIBuilder setGlow(boolean glow) {
        this.glow = glow;
        return this;
    }

    /**
     * Sets the location of the sign. Set to null for the default location which is a few blocks behind the player.
     *
     * @param loc The location.
     * @return The {@link SignGUIBuilder} instance
     */
    public SignGUIBuilder setLocation(Location loc) {
        this.loc = loc;
        return this;
    }

    /**
     * Sets the handler that is called when the player finishes the sign gui.
     * See {@link SignGUIFinishHandler#onFinish(Player, SignGUIResult)} for more information.
     *
     * @param handler The handler.
     * @return The {@link SignGUIBuilder} instance
     * @throws java.lang.IllegalArgumentException If handler is null.
     */
    public SignGUIBuilder setHandler(SignGUIFinishHandler handler) {
        Validate.notNull(handler, "The handler cannot be null");
        this.handler = handler;
        return this;
    }

    /**
     * If called the handler will be called synchronously by calling the method {@link org.bukkit.scheduler.BukkitScheduler#runTask(Plugin, Runnable)}
     *
     * @param plugin Your {@link org.bukkit.plugin.java.JavaPlugin} instance.
     * @return The {@link SignGUIBuilder} instance
     */
    public SignGUIBuilder callHandlerSynchronously(JavaPlugin plugin) {
        this.callHandlerSynchronously = true;
        this.plugin = plugin;
        return this;
    }

    /**
     * Builds the SignGUI.
     *
     * @return The SignGUI.
     */
    public SignGUI build() {
        Validate.notNull(handler, "handler must be set");
        return new SignGUI(lines, adventureLines, type, color, glow, loc, handler, callHandlerSynchronously, plugin);
    }
}
