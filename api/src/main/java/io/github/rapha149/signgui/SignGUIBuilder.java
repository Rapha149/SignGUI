package io.github.rapha149.signgui;

import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Builder for {@link SignGUI}.
 */
public class SignGUIBuilder {

    private String[] lines = new String[4];
    private Material type = SignGUI.WRAPPER.getDefaultType();
    private DyeColor color = DyeColor.BLACK;
    private Location loc;
    private SignGUIFinishHandler handler;
    private boolean callHandlerSynchronously = false;
    private JavaPlugin plugin;

    /**
     * Constructs a new SignGUIBuilder. Use {@link SignGUI#builder()} to get a new instance.
     */
    SignGUIBuilder() {
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
        this.lines = lines;
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
     * Sets the type of the sign.
     *
     * @param type The type. Must be a sign type.
     * @return The {@link SignGUIBuilder} instance
     * @throws java.lang.IllegalArgumentException If type is null or not a sign type.
     */
    public SignGUIBuilder setType(Material type) {
        Validate.notNull(type, "The type cannot be null");
        Validate.isTrue(SignGUI.WRAPPER.getSignTypes().contains(type), type + " is not a sign type. Available sign types: " + SignGUI.availableSignTypes);
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
        return new SignGUI(lines, type, color, loc, handler, callHandlerSynchronously, plugin);
    }
}
