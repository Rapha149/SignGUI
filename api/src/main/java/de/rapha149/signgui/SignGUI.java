package de.rapha149.signgui;

import de.rapha149.signgui.version.VersionWrapper;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SignGUI {

    private static final VersionWrapper WRAPPER;
    private static final List<String> signTypes = Arrays.asList("OAK_SIGN", "BIRCH_SIGN",
            "SPRUCE_SIGN", "JUNGLE_SIGN", "ACACIA_SIGN", "DARK_OAK_SIGN", "CRIMSON_SIGN", "WARPED_SIGN");

    static {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
        try {
            WRAPPER = (VersionWrapper) Class.forName(VersionWrapper.class.getPackage().getName() + ".Wrapper" + version).newInstance();
        } catch (IllegalAccessException | InstantiationException exception) {
            throw new IllegalStateException("Failed to load support for server version " + version, exception);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("SignGUI does not support the server version \"" + version + "\"", exception);
        }
    }

    /**
     * The lines to show.
     */
    private String[] lines;

    /**
     * The sign type.
     */
    private Material type;

    /**
     * The color of the sign (1.14+)
     */
    private DyeColor color;

    /**
     * The {@link java.util.function.BiFunction} which will be executed when the editing is finished. If new lines are returned, the new lines are opened to edit.
     */
    private BiFunction<Player, String[], String[]> function;

    /**
     * Constructs a new SignGUI.
     */
    public SignGUI() {
        lines = new String[4];
        type = Material.OAK_SIGN;
        color = DyeColor.BLACK;
    }

    /**
     * Sets the lines that are shown on the sign.
     *
     * @param lines The lines, may be less than 4.
     * @return The {@link de.rapha149.signgui.SignGUI} instance
     */
    public SignGUI lines(String... lines) {
        this.lines = Arrays.copyOf(lines, 4);
        return this;
    }

    /**
     * Sets a specific line that is shown on the sign.
     *
     * @param index The index of the line.
     * @param line  The line.
     * @return The {@link de.rapha149.signgui.SignGUI} instance
     * @throws java.lang.IllegalArgumentException If the index is below 0 or above 4.
     */
    public SignGUI line(int index, String line) {
        Validate.isTrue(index >= 0 && index <= 4, "Index out of range");
        lines[index] = line;
        return this;
    }

    /**
     * Sets the type of the sign.
     *
     * @param type The type. Must be a sign type.
     * @return The {@link de.rapha149.signgui.SignGUI} instance
     */
    public SignGUI type(Material type) {
        Validate.notNull(type, "Type cannot be null");
        Validate.isTrue(signTypes.contains(type.toString()), "Type is not a sign type.");
        this.type = type;
        return this;
    }

    /**
     * Sets the color of the sign (1.14+)
     *
     * @param color The new color
     * @return The {@link de.rapha149.signgui.SignGUI} instance
     */
    public SignGUI color(DyeColor color) {
        Validate.notNull(color, "The color cannot be null");
        this.color = color;
        return this;
    }

    /**
     * Sets the {@link java.util.function.Consumer} which will be executed when the editing is finished. If new lines are returned, the new lines are opened to edit.
     * Will override {@link de.rapha149.signgui.SignGUI#onFinish(java.util.function.BiFunction)}
     *
     * @param function The function.
     * @return The {@link de.rapha149.signgui.SignGUI} instance
     */
    public SignGUI onFinish(Function<String[], String[]> function) {
        Validate.notNull(function, "The function cannot be null.");
        this.function = (player, lines) -> function.apply(lines);
        return this;
    }

    /**
     * Sets the {@link java.util.function.BiFunction} which will be executed when the editing is finished. If new lines are returned, the new lines are opened to edit.
     * Will override {@link de.rapha149.signgui.SignGUI#onFinish(java.util.function.Function)}
     *
     * @param function The function.
     * @return The {@link de.rapha149.signgui.SignGUI} instance
     */
    public SignGUI onFinish(BiFunction<Player, String[], String[]> function) {
        Validate.notNull(function, "The function cannot be null.");
        this.function = function;
        return this;
    }

    /**
     * Constructs and opens the sign gui for the player.
     *
     * @param player The player.
     * @return The {@link de.rapha149.signgui.SignGUI} instance
     */
    public SignGUI open(Player player) {
        Validate.notNull(player, "The player cannot be null");
        Validate.notNull(type, "Type cannot be null");
        Validate.isTrue(signTypes.contains(type.toString()), "The type is not a sign type.");
        Validate.notNull(color, "The color cannot be null");
        Validate.notNull(function, "The function cannot be null.");
        WRAPPER.openSignEditor(player, lines, type, color, function);
        return this;
    }
}
