package de.rapha149.signgui;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

/**
 * An interface used for handling the action after the player finished editing the sign.
 */
public interface SignGUIAction {

    SignGUIActionInfo getInfo();

    /**
     * Called to execute the actions after the player finished editing the sign.
     *
     * @param gui    The {@link de.rapha149.signgui.SignGUI} instance
     * @param player The player who edited the sign
     */
    void execute(SignGUI gui, SignEditor signEditor, Player player);

    /**
     * Creates a new SignGUIAction that opens the sign gui again with the new lines.
     *
     * @param lines The new lines, may be less then 4
     * @return The new {@link de.rapha149.signgui.SignGUIAction} instance
     * @throws java.lang.IllegalArgumentException If lines is null.
     */
    static SignGUIAction displayNewLines(String... lines) {
        Validate.notNull(lines, "The lines cannot be null");

        return new SignGUIAction() {

            private SignGUIActionInfo info = new SignGUIActionInfo("displayNewLines", true, 1);

            @Override
            public SignGUIActionInfo getInfo() {
                return info;
            }

            @Override
            public void execute(SignGUI gui, SignEditor signEditor, Player player) {
                gui.displayNewLines(player, signEditor, Arrays.copyOf(lines, 4));
            }
        };
    }

    /**
     * Creates a new SignGUIAction that opens an inventory.
     * The inventory is opened synchronously by calling the method {@link org.bukkit.scheduler.BukkitScheduler#runTask(Plugin, Runnable)}
     *
     * @param plugin    Your {@link org.bukkit.plugin.java.JavaPlugin} instance
     * @param inventory The inventory to open
     * @return The new {@link de.rapha149.signgui.SignGUIAction} instance
     */
    static SignGUIAction openInventory(JavaPlugin plugin, Inventory inventory) {
        Validate.notNull(plugin, "The plugin cannot be null");
        Validate.notNull(inventory, "The inventory cannot be null");
        return new SignGUIAction() {

            private SignGUIActionInfo info = new SignGUIActionInfo("openInventory", false, 1);

            @Override
            public SignGUIActionInfo getInfo() {
                return info;
            }

            @Override
            public void execute(SignGUI gui, SignEditor signEditor, Player player) {
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(inventory));
            }
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
        return new SignGUIAction() {

            private SignGUIActionInfo info = new SignGUIActionInfo("run", false, 0);

            @Override
            public SignGUIActionInfo getInfo() {
                return info;
            }

            @Override
            public void execute(SignGUI gui, SignEditor signEditor, Player player) {
                runnable.run();
            }
        };
    }

    /**
     * Describes a {@link de.rapha149.signgui.SignGUIAction}
     */
    class SignGUIActionInfo {

        private final String name;
        private final boolean keepOpen;
        private final int conflicting;

        /**
         * Creates a new SignGUIActionInfo.
         *
         * @param name        The name of the action
         * @param keepOpen    Whether the sign gui should be kept open
         * @param conflicting The conflicting int
         */
        public SignGUIActionInfo(String name, boolean keepOpen, int conflicting) {
            this.name = name;
            this.keepOpen = keepOpen;
            this.conflicting = conflicting;
        }

        /**
         * @return The name of the action
         */
        public String getName() {
            return name;
        }

        /**
         * @return Whether the sign gui should be kept open.
         */
        public boolean isKeepOpen() {
            return keepOpen;
        }

        /**
         * Checks whether the result is conflicting with another result.
         *
         * @param other The conflicting int of the other result
         * @return Whether the result is conflicting with the other result
         */
        public boolean isConflicting(SignGUIActionInfo other) {
            return (conflicting & other.conflicting) != 0;
        }
    }
}
