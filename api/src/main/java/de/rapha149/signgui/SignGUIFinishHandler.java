package de.rapha149.signgui;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * A functional interface used for handling the result of the sign editing.
 */
@FunctionalInterface
public interface SignGUIFinishHandler {

    /**
     * Called when the player finished editing the sign.
     * @param player The player who edited the sign.
     * @param result The result of the editing.
     * @return A list of actions that should be executed after the editing is finished. The actions are executed in the order they are in the list.
     */
    List<SignGUIAction> onFinish(Player player, SignGUIResult result);
}
