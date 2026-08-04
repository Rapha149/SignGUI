package de.rapha149.signgui.version;

import de.rapha149.signgui.exception.SignGUIVersionException;

/**
 * Probes the running server's Minecraft protocol version.
 */
public final class ProtocolProbe {

    private ProtocolProbe() {
    }

    /**
     * @return the current protocol version
     * @throws SignGUIVersionException if the protocol cannot be determined
     */
    public static int current() throws SignGUIVersionException {
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

        try {
            Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
            Object server = bukkit.getMethod("getServer").invoke(null);
            String pkg = server.getClass().getPackage().getName();
            int idx = pkg.lastIndexOf('.');
            if (idx >= 0) {
                Integer mapped = mapLegacyPackage(pkg.substring(idx + 1));
                if (mapped != null) {
                    return mapped;
                }
            }
        } catch (Throwable ignored) {
        }

        throw new SignGUIVersionException("Cannot probe protocol version");
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
            default:
                return null;
        }
    }
}
