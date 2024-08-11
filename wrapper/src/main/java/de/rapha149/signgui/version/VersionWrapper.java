package de.rapha149.signgui.version;

import de.rapha149.signgui.SignEditor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;

/**
 * Interface for version wrappers
 */
public interface VersionWrapper {

    ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(0);

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
     * @param glow  If the sign's text should glow (1.17+)
     * @param signLoc  The location where the sign should be placed. Can be null for default.
     * @param onFinish The {@link java.util.function.BiConsumer} which is called when the player finished editing the sign.
     */
    void openSignEditor(Player player, String[] lines, Object[] adventureLines, Material type, DyeColor color, boolean glow, Location signLoc, BiConsumer<SignEditor, String[]> onFinish) throws Exception;

    /**
     * Called when the lines of a sign should be updated.
     *
     * @param player The player to whom the sign was shown to.
     * @param signEditor The sign editor.
     * @param lines The new lines.
     */
    void displayNewLines(Player player, SignEditor signEditor, String[] lines, Object[] adventureLines);

    /**
     * Called when the sign editor should be closed.
     *
     * @param player The player to whom the sign was shown to.
     * @param signEditor The sign editor.
     */
    void closeSignEditor(Player player, SignEditor signEditor);

    /**
     * Get the default location where the sign should be placed for the player.
     *
     * @param player The player.
     * @return The location of the sign.
     */
    default Location getDefaultLocation(Player player) {
        Location loc = player.getEyeLocation();
        return loc.clone().add(loc.getDirection().multiply(-3));
    }
}
