package de.rapha149.signgui;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

/**
 * A functional interface used for handling the action after the player finished editing the sign.
 */
@FunctionalInterface
public interface SignGUIAction {

    /**
     * Called to execute the actions after the player finished editing the sign.
     * @param gui The {@link de.rapha149.signgui.SignGUI} instance
     * @param player The player who edited the sign
     * @return true if the sign gui should be kept open, false otherwise
     */
    boolean execute(SignGUI gui, SignEditor signEditor, Player player);

    /**
     * Creates a new SignGUIAction that opens the sign gui again with the new lines.
     * @param lines The new lines, may be less then 4
     * @return The new {@link de.rapha149.signgui.SignGUIAction} instance
     * @throws java.lang.IllegalArgumentException If lines is null.
     */
    static SignGUIAction displayNewLines(String... lines) {
        Validate.notNull(lines, "The lines cannot be null");

        return (gui, signEditor, player) -> {
            gui.displayNewLines(player, signEditor, Arrays.copyOf(lines, 4));
            return true;
        };
    }

    /**
     * Creates a new SignGUIAction that opens an inventory.
     * The inventory is opened synchronously by calling the method {@link org.bukkit.scheduler.BukkitScheduler#runTask(Plugin, Runnable)}
     *
     * @param plugin Your {@link org.bukkit.plugin.java.JavaPlugin} instance
     * @param inventory The inventory to open
     * @return The new {@link de.rapha149.signgui.SignGUIAction} instance
     */
    static SignGUIAction openInventory(JavaPlugin plugin, Inventory inventory) {
        Validate.notNull(plugin, "The plugin cannot be null");
        Validate.notNull(inventory, "The inventory cannot be null");
        return (gui, signEditor, player) -> {
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(inventory));
            return false;
        };
    }

    /**
     * Creates a new SignGUIAction that runs a runnable.
     * The runnable will be run asynchronously.
     *
     * @param runnable The runnable to run
     * @return The new {@link de.rapha149.signgui.SignGUIAction} instance
     */
    static SignGUIAction run(Runnable runnable) {
        Validate.notNull(runnable, "The runnable cannot be null");
        return (gui, signEditor, player) -> {
            runnable.run();
            return false;
        };
    }
}
