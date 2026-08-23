package de.rapha149.signgui.version;

import de.rapha149.signgui.exception.SignGUIVersionException;
import org.bukkit.Bukkit;

/**
 * Probes the running server's Minecraft protocol version.
 * <p>
 * Prefers {@code SharedConstants}, then CraftBukkit package mapping, then Bukkit version string.
 */
public final class ProtocolProbe {

    private ProtocolProbe() {
    }

    /**
     * @return the current protocol version
     * @throws SignGUIVersionException if the protocol cannot be determined
     */
    public static int current() throws SignGUIVersionException {
        Integer fromShared = fromSharedConstants();
        if (fromShared != null) {
            return fromShared;
        }

        Integer fromPackage = fromCraftBukkitPackage();
        if (fromPackage != null) {
            return fromPackage;
        }

        Integer fromBukkit = fromBukkitVersion();
        if (fromBukkit != null) {
            return fromBukkit;
        }

        throw new SignGUIVersionException("Cannot probe protocol version");
    }

    private static Integer fromSharedConstants() {
        try {
            Class<?> shared = Class.forName("net.minecraft.SharedConstants");
            Object version = shared.getMethod("getCurrentVersion").invoke(null);
            try {
                Object protocol = version.getClass().getMethod("getProtocolVersion").invoke(version);
                if (protocol instanceof Integer) {
                    return (Integer) protocol;
                }
            } catch (NoSuchMethodException ignored) {
            }
            try {
                Object protocol = version.getClass().getMethod("protocolVersion").invoke(version);
                if (protocol instanceof Integer) {
                    return (Integer) protocol;
                }
            } catch (NoSuchMethodException ignored) {
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static Integer fromCraftBukkitPackage() {
        try {
            String pkg = Bukkit.getServer().getClass().getPackage().getName();
            int idx = pkg.lastIndexOf('.');
            if (idx >= 0) {
                return mapLegacyPackage(pkg.substring(idx + 1));
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static Integer fromBukkitVersion() {
        try {
            String version = normalizeMinecraftVersion(Bukkit.getBukkitVersion());
            Integer exact = mapMinecraftVersion(version);
            if (exact != null) {
                return exact;
            }
            int lastDot = version.lastIndexOf('.');
            int firstDot = version.indexOf('.');
            if (lastDot > firstDot) {
                return mapMinecraftVersion(version.substring(0, lastDot));
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static String normalizeMinecraftVersion(String bukkitVersion) {
        String version = bukkitVersion;
        int dash = version.indexOf('-');
        if (dash >= 0) {
            version = version.substring(0, dash);
        }
        int buildIdx = version.indexOf(".build.");
        if (buildIdx >= 0) {
            version = version.substring(0, buildIdx);
        }
        return version;
    }

    private static Integer mapMinecraftVersion(String version) {
        if (version == null) {
            return null;
        }
        switch (version) {
            case "1.8":
            case "1.8.3":
            case "1.8.4":
            case "1.8.5":
            case "1.8.6":
            case "1.8.7":
            case "1.8.8":
            case "1.8.9":
                return ProtocolVersions.V1_8;
            case "1.9":
            case "1.9.2":
                return ProtocolVersions.V1_9;
            case "1.9.4":
                return ProtocolVersions.V1_9_4;
            case "1.10":
            case "1.10.2":
                return ProtocolVersions.V1_10;
            case "1.11":
            case "1.11.2":
                return ProtocolVersions.V1_11;
            case "1.12":
            case "1.12.2":
                return ProtocolVersions.V1_12_2;
            case "1.13":
            case "1.13.1":
                return ProtocolVersions.V1_13;
            case "1.13.2":
                return ProtocolVersions.V1_13_2;
            case "1.14":
            case "1.14.4":
                return ProtocolVersions.V1_14_4;
            case "1.15":
            case "1.15.2":
                return ProtocolVersions.V1_15_2;
            case "1.16":
            case "1.16.1":
                return ProtocolVersions.V1_16_1;
            case "1.16.2":
            case "1.16.3":
                return ProtocolVersions.V1_16_3;
            case "1.16.4":
            case "1.16.5":
                return ProtocolVersions.V1_16_4;
            case "1.17":
            case "1.17.1":
                return ProtocolVersions.V1_17_1;
            case "1.18":
            case "1.18.1":
                return ProtocolVersions.V1_18;
            case "1.18.2":
                return ProtocolVersions.V1_18_2;
            case "1.19":
            case "1.19.1":
            case "1.19.2":
                return ProtocolVersions.V1_19_1;
            case "1.19.3":
                return ProtocolVersions.V1_19_3;
            case "1.19.4":
                return ProtocolVersions.V1_19_4;
            case "1.20":
            case "1.20.1":
                return ProtocolVersions.V1_20;
            case "1.20.2":
                return ProtocolVersions.V1_20_2;
            case "1.20.3":
            case "1.20.4":
                return ProtocolVersions.V1_20_3;
            case "1.20.5":
            case "1.20.6":
                return ProtocolVersions.V1_20_5;
            case "1.21":
            case "1.21.1":
                return ProtocolVersions.V1_21;
            case "1.21.2":
            case "1.21.3":
                return ProtocolVersions.V1_21_2;
            case "1.21.4":
                return ProtocolVersions.V1_21_4;
            case "1.21.5":
                return ProtocolVersions.V1_21_5;
            case "1.21.6":
                return ProtocolVersions.V1_21_6;
            case "1.21.7":
            case "1.21.8":
                return ProtocolVersions.V1_21_7;
            case "1.21.9":
            case "1.21.10":
                return ProtocolVersions.V1_21_9;
            case "1.21.11":
                return ProtocolVersions.V1_21_11;
            case "26.1":
            case "26.1.1":
            case "26.1.2":
                return ProtocolVersions.V26_1;
            case "26.2":
                return ProtocolVersions.V26_2;
            default:
                return null;
        }
    }

    private static Integer mapLegacyPackage(String nms) {
        switch (nms) {
            case "v1_8_R1":
            case "v1_8_R2":
            case "v1_8_R3":
                return ProtocolVersions.V1_8;
            case "v1_9_R1":
                return ProtocolVersions.V1_9;
            case "v1_9_R2":
                return ProtocolVersions.V1_9_4;
            case "v1_10_R1":
                return ProtocolVersions.V1_10;
            case "v1_11_R1":
                return ProtocolVersions.V1_11;
            case "v1_12_R1":
                return ProtocolVersions.V1_12_2;
            case "v1_13_R1":
                return ProtocolVersions.V1_13;
            case "v1_13_R2":
                return ProtocolVersions.V1_13_2;
            case "v1_14_R1":
                return ProtocolVersions.V1_14_4;
            case "v1_15_R1":
                return ProtocolVersions.V1_15_2;
            case "v1_16_R1":
                return ProtocolVersions.V1_16_1;
            case "v1_16_R2":
                return ProtocolVersions.V1_16_3;
            case "v1_16_R3":
                return ProtocolVersions.V1_16_4;
            case "v1_17_R1":
                return ProtocolVersions.V1_17_1;
            case "v1_18_R1":
                return ProtocolVersions.V1_18;
            case "v1_18_R2":
                return ProtocolVersions.V1_18_2;
            case "v1_19_R1":
                return ProtocolVersions.V1_19_1;
            case "v1_19_R2":
                return ProtocolVersions.V1_19_3;
            case "v1_19_R3":
                return ProtocolVersions.V1_19_4;
            case "v1_20_R1":
                return ProtocolVersions.V1_20;
            case "v1_20_R2":
                return ProtocolVersions.V1_20_2;
            case "v1_20_R3":
                return ProtocolVersions.V1_20_3;
            case "v1_20_R4":
                return ProtocolVersions.V1_20_5;
            case "v1_21_R1":
                return ProtocolVersions.V1_21;
            case "v1_21_R2":
                return ProtocolVersions.V1_21_2;
            case "v1_21_R3":
                return ProtocolVersions.V1_21_4;
            case "v1_21_R4":
                return ProtocolVersions.V1_21_5;
            case "v1_21_R5":
                return ProtocolVersions.V1_21_6;
            case "v1_21_R6":
                return ProtocolVersions.V1_21_9;
            case "v1_21_R7":
                return ProtocolVersions.V1_21_11;
            default:
                return null;
        }
    }
}
