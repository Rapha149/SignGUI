package de.rapha149.signgui.version;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayInUpdateSign;
import net.minecraft.network.protocol.game.PacketPlayOutOpenSignEditor;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.TileEntitySign;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class Wrapper1_20_R1 implements VersionWrapper {

    private final Field NETWORK_MANAGER_FIELD;

    {
        Field networkManagerField = null;
        for (Field field : PlayerConnection.class.getDeclaredFields()) {
            if (field.getType() == NetworkManager.class) {
                field.setAccessible(true);
                networkManagerField = field;
                break;
            }
        }

        NETWORK_MANAGER_FIELD = networkManagerField;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Material getDefaultType() {
        return Material.OAK_SIGN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Material> getSignTypes() {
        return Arrays.asList(Material.OAK_SIGN, Material.BIRCH_SIGN, Material.SPRUCE_SIGN, Material.JUNGLE_SIGN,
                Material.ACACIA_SIGN, Material.DARK_OAK_SIGN, Material.CRIMSON_SIGN, Material.WARPED_SIGN,
                Material.CHERRY_SIGN, Material.MANGROVE_SIGN, Material.BAMBOO_SIGN
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openSignEditor(Player player, String[] lines, Material type, DyeColor color, Location signLoc, BiFunction<Player, String[], String[]> function) {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        PlayerConnection conn = p.c;

        if (NETWORK_MANAGER_FIELD == null)
            throw new IllegalStateException("Unable to find NetworkManager field in PlayerConnection class.");
        if (!NETWORK_MANAGER_FIELD.canAccess(conn)) {
            NETWORK_MANAGER_FIELD.setAccessible(true);
            if (!NETWORK_MANAGER_FIELD.canAccess(conn)) {
                throw new IllegalStateException("Unable to access NetworkManager field in PlayerConnection class.");
            }
        }

        Location loc = signLoc != null ? signLoc : getDefaultLocation(player);
        BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        TileEntitySign sign = new TileEntitySign(pos, null);
        SignText signText = sign.a(true) // flag = front/back of sign
                .a(EnumColor.valueOf(color.toString()));
        for (int i = 0; i < lines.length; i++)
            signText = signText.a(i, IChatBaseComponent.a(lines[i]));
        sign.a(signText, true);

        player.sendBlockChange(loc, type.createBlockData());
        conn.a(sign.j());
        conn.a(new PacketPlayOutOpenSignEditor(pos, true)); // flag = front/back of sign

        NetworkManager manager;
        try {
            manager = (NetworkManager) NETWORK_MANAGER_FIELD.get(conn);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        ChannelPipeline pipeline = manager.m.pipeline();
        if (pipeline.names().contains("SignGUI")) {
            pipeline.remove("SignGUI");
        }
        pipeline.addAfter("decoder", "SignGUI", new MessageToMessageDecoder<Packet<?>>() {
            @Override
            protected void decode(ChannelHandlerContext chc, Packet<?> packet, List<Object> out) {
                try {
                    if (packet instanceof PacketPlayInUpdateSign updateSign) {
                        if (updateSign.a().equals(pos)) {
                            String[] response = function.apply(player, updateSign.d());
                            if (response != null) {
                                String[] newLines = Arrays.copyOf(response, 4);
                                SignText newSignText = sign.a(true);
                                for (int i = 0; i < newLines.length; i++)
                                    newSignText = newSignText.a(i, IChatBaseComponent.a(newLines[i]));
                                sign.a(newSignText, true);
                                conn.a(sign.j());
                                conn.a(new PacketPlayOutOpenSignEditor(pos, true));
                            } else {
                                pipeline.remove("SignGUI");
                                player.sendBlockChange(loc, loc.getBlock().getBlockData());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                out.add(packet);
            }
        });
    }
}
