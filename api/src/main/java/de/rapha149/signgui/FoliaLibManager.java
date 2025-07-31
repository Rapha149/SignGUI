package de.rapha149.signgui;

import com.tcoded.folialib.FoliaLib;
import org.bukkit.plugin.java.JavaPlugin;


public class FoliaLibManager {

    private static FoliaLib foliaLib;
    public static boolean initialized = false;

    public static void initialize(JavaPlugin plugin) {
        if (initialized) {
            throw new IllegalStateException("FoliaLibManager is already initialized");
        }

        foliaLib = new FoliaLib(plugin);
        initialized = true;
    }

    public static FoliaLib getFoliaLib() {
        if (!initialized) {
            throw new IllegalStateException("FoliaLibManager is not initialized");
        }
        return foliaLib;
    }
}