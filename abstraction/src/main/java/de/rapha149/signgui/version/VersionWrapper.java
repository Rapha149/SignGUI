package de.rapha149.signgui.version;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Interface for version wrappers
 */
public interface VersionWrapper {

    /**
     * @return The default type for the sign. Different between some versions.
     */
    Material getDefaultType();

    /**
     * @return A list of available sign types. Different between some versions.
     */
    List<Material> getSignTypes();

    /**
     * Called when a sign gui should be opened.
     *
     * @param player   The player to whom the sign is to be shown.
     * @param lines    The lines that are shown.
     * @param type     The type of the sign.
     * @param color    The color of the sign (1.14+)
     * @param signLoc  The location where the sign should be placed. Can be null for default. See {@link de.rapha149.signgui.version.VersionWrapper#getLocation(org.bukkit.entity.Player, int)}
     * @param function The {@link java.util.function.BiFunction} which is executed when the editing is finished. If new lines are returned, the new lines are opened to edit.
     */
    void openSignEditor(Player player, String[] lines, Material type, DyeColor color, Location signLoc, BiFunction<Player, String[], String[]> function) throws Exception;

    /**
     * Get the location where the sign should be placed for the player.
     *
     * @param player The player.
     * @return The location of the sign (default y = 1 or y = -63)
     */
    default Location getLocation(Player player, int y) {
        Location loc = player.getLocation().clone();
        loc.setY(y);
        return loc;
    }
}
