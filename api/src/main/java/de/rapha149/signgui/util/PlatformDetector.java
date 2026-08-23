package de.rapha149.signgui.util;

import org.bukkit.Bukkit;

/**
 * Utility class for detecting the server platform.
 */
public class PlatformDetector {
    
    private static PlatformType detectedPlatform;
    private static boolean hasFoliaClasses;
    private static boolean hasRegionScheduler;
    
    static {
        detectPlatform();
    }
    
    /**
     * Detects the server platform.
     */
    private static void detectPlatform() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            hasFoliaClasses = true;
        } catch (ClassNotFoundException ignored) {
            hasFoliaClasses = false;
        }
        
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            hasRegionScheduler = true;
        } catch (ClassNotFoundException ignored) {
            hasRegionScheduler = false;
        }
        
        String serverVersion = Bukkit.getVersion();
        String serverName = Bukkit.getName();
        
        if (hasFoliaClasses && hasRegionScheduler) {
            detectedPlatform = PlatformType.FOLIA;
        } else if (serverVersion.contains("Canvas") || serverName.contains("Canvas")) {
            detectedPlatform = PlatformType.CANVAS;
        } else if (serverVersion.contains("Archlight") || serverName.contains("Archlight")) {
            detectedPlatform = PlatformType.ARCHLIGHT;
        } else {
            detectedPlatform = PlatformType.BUKKIT;
        }
    }
    
    /**
     * Gets the detected platform type.
     *
     * @return The detected platform type
     */
    public static PlatformType getPlatformType() {
        return detectedPlatform;
    }
    
    /**
     * Checks if the server is running on Folia.
     *
     * @return true if running on Folia, false otherwise
     */
    public static boolean isFolia() {
        return detectedPlatform == PlatformType.FOLIA;
    }
    
    /**
     * Checks if the server is running on CanvasMC.
     *
     * @return true if running on CanvasMC, false otherwise
     */
    public static boolean isCanvas() {
        return detectedPlatform == PlatformType.CANVAS;
    }
    
    /**
     * Checks if the server is running on Archlight.
     *
     * @return true if running on Archlight, false otherwise
     */
    public static boolean isArchlight() {
        return detectedPlatform == PlatformType.ARCHLIGHT;
    }
    
    /**
     * Checks if the server supports region-based scheduling (Folia).
     *
     * @return true if region scheduling is supported, false otherwise
     */
    public static boolean hasRegionScheduler() {
        return hasRegionScheduler;
    }
    
    /**
     * Checks if the server has Folia classes available.
     *
     * @return true if Folia classes are available, false otherwise
     */
    public static boolean hasFoliaClasses() {
        return hasFoliaClasses;
    }
}
