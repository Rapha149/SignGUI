package de.rapha149.signgui.version;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Interface for version wrappers
 */
public interface VersionWrapper {

    /**
     * Called when a sign gui should be opened.
     *
     * @param player   The player to whom the sign is to be shown.
     * @param lines    The lines that are shown.
     * @param type     The type of the sign.
     * @param color    The color of the sign (1.14+)
     * @param function The {@link java.util.function.BiFunction} which is executed when the editing is finished. If new lines are returned, the new lines are opened to edit.
     */
    void openSignEditor(Player player, String[] lines, Material type, DyeColor color, BiFunction<Player, String[], String[]> function) throws Exception;

    /**
     * Get the location where the sign should be placed for the player.
     *
     * @param player The player.
     * @return The location of the sign (default behind the player)
     */
    default Location getLocation(Player player) {
        return player.getLocation().clone().add(player.getLocation().getDirection().normalize().multiply(-1));
    }
}
