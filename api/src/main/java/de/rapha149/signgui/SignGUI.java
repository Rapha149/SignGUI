package de.rapha149.signgui;

import de.rapha149.signgui.SignGUIAction.SignGUIActionInfo;
import de.rapha149.signgui.exception.SignGUIException;
import de.rapha149.signgui.exception.SignGUIVersionException;
import de.rapha149.signgui.version.VersionMatcher;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.function.Consumer;

/**
 * The base class of this api. Use {@link SignGUI#builder()} to get a new instance.
 */
public class SignGUI {

    /**
     * Constructs a new SignGUIBuilder.
     *
     * @return The new {@link SignGUIBuilder} instance
     * @throws SignGUIVersionException If the server version is not supported by this api.
     */
    public static void builderAsync(Consumer<SignGUIBuilder> callback, Consumer<Exception> errorCallback) {
        VersionMatcher.getWrapperAsync(wrapper -> {
            // Once the wrapper is successfully loaded, create and pass the builder.
            callback.accept(new SignGUIBuilder(wrapper));
        }, errorCallback);
    }

    private final String[] lines;
    private final Object[] adventureLines;
    private final Material type;
    private final DyeColor color;
    private final boolean glow;
    private final Location signLoc;
    private final SignGUIFinishHandler handler;
    private final boolean callHandlerSynchronously;
    public static final JavaPlugin plugin;

    /**
     * Constructs a new SignGUI. Use {@link SignGUI#builder()} to get a new instance.
     */
    SignGUI(String[] lines, Object[] adventureLines, Material type, DyeColor color, boolean glow, Location signLoc, SignGUIFinishHandler handler, boolean callHandlerSynchronously, JavaPlugin plugin) {
        this.lines = lines;
        this.adventureLines = adventureLines;
        this.type = type;
        this.color = color;
        this.glow = glow;
        this.signLoc = signLoc;
        this.handler = handler;
        this.callHandlerSynchronously = callHandlerSynchronously;
        this.plugin = plugin;
    }

    /**
     * Opens the sign gui for the player.
     * <p>
     * Note: if there already is a sign gui open for the player, it will be closed and the {@link SignGUIFinishHandler} will not be called.
     * It is recommended to avoid opening a sign gui for a player that already has one open.
     *
     * @param player The player to open the gui for.
     * @throws de.rapha149.signgui.exception.SignGUIException If an error occurs while opening the gui.
     */
    public void open(Player player) throws SignGUIException {
        Validate.notNull(player, "The player cannot be null");

        try {
            VersionMatcher.getWrapperAsync(wrapper -> {
                try {
                    wrapper.openSignEditor(player, lines, adventureLines, type, color, glow, signLoc,
                            (signEditor, resultLines) -> {
                                Runnable runnable = () -> {
                                    Runnable close = () -> {
                                        wrapper.closeSignEditor(player, signEditor);
                                    };

                                    List<SignGUIAction> actions = handler.onFinish(player, new SignGUIResult(resultLines));

                                    if (actions == null || actions.isEmpty()) {
                                        close.run();
                                        return;
                                    }

                                    boolean keepOpen = false;
                                    for (SignGUIAction action : actions) {
                                        SignGUIActionInfo info = action.getInfo();
                                        for (SignGUIAction otherAction : actions) {
                                            if (action == otherAction)
                                                continue;

                                            SignGUIActionInfo otherInfo = otherAction.getInfo();
                                            if (info.isConflicting(otherInfo)) {
                                                close.run();
                                                throw new IllegalArgumentException("The actions " + info.getName() + " and " + otherInfo.getName() + " are conflicting");
                                            }
                                        }

                                        if (info.isKeepOpen())
                                            keepOpen = true;
                                    }

                                    if (!keepOpen)
                                        close.run();

                                    for (SignGUIAction action : actions)
                                        action.execute(this, signEditor, player);
                                };

                                // Execute the runnable either synchronously on the main thread or immediately in the current thread.
                                if (callHandlerSynchronously)
                                    Bukkit.getScheduler().runTask(SignGUI.plugin, runnable);
                                else
                                    runnable.run();
                            }
                    );
                } catch (Exception e) {
                    throw new SignGUIException("Failed to open sign gui", e);
                }
            }, error -> {
                throw new SignGUIException("Failed to initialize version wrapper", error);
            });
        } catch (Exception e) {
            throw new SignGUIException("Failed to open sign gui", e);
        }
    }

    /**
     * Sets the lines that are shown on the sign.
     * This is called when {@link SignGUIAction#displayNewLines(String...)} is returned as an action after the player has finished editing.
     *
     * @param lines The lines, must be exactly 4.
     * @param adventureLines The lines using Adventure components (1.20.5+). Must be exactly 4. May be null.
     * @throws java.lang.IllegalArgumentException If lines is null or not exactly 4 lines.
     * @throws SignGUIException                   If an error occurs while setting the lines.
     */
    void displayNewLines(Player player, SignEditor signEditor, String[] lines, Object[] adventureLines) {
        Validate.notNull(lines, "The lines cannot be null");
        Validate.isTrue(lines.length == 4, "The lines must have a length of 4");
        if (adventureLines != null) {
            Validate.isTrue(adventureLines.length == 4, "The adventure lines must null or have a length of 4");
        }

        VersionMatcher.getWrapperAsync(
                wrapper -> {
                    try {
                        wrapper.displayNewLines(player, signEditor, lines, adventureLines);
                    } catch (Exception e) {
                        throw new SignGUIException("Failed to display new lines", e);
                    }
                },
                error -> {
                    throw new SignGUIException("Failed to initialize version wrapper asynchronously", error);
                }
        );
    }
}
