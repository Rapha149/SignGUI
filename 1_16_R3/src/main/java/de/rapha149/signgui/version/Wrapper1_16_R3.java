package de.rapha149.signgui.version;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftSign;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class Wrapper1_16_R3 implements VersionWrapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public void openSignEditor(Player player, String[] lines, Material type, DyeColor color, BiFunction<Player, String[], String[]> function) {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        PlayerConnection conn = p.playerConnection;
        Location loc = getLocation(player);
        BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        TileEntitySign sign = new TileEntitySign();
        sign.setPosition(pos);
        sign.setColor(EnumColor.valueOf(color.toString()));
        IChatBaseComponent[] sanitizedLines = CraftSign.sanitizeLines(lines);
        for (int i = 0; i < sanitizedLines.length; i++)
            sign.a(i, sanitizedLines[i]);

        conn.sendPacket(new PacketPlayOutBlockChange(pos, getBlockData(type)));
        conn.sendPacket(sign.getUpdatePacket());
        conn.sendPacket(new PacketPlayOutOpenSignEditor(pos));

        ChannelPipeline pipeline = conn.networkManager.channel.pipeline();
        if (pipeline.names().contains("SignGUI"))
            pipeline.remove("SignGUI");
        pipeline.addAfter("decoder", "SignGUI", new MessageToMessageDecoder<Packet<?>>() {
            @Override
            protected void decode(ChannelHandlerContext chc, Packet<?> packet, List<Object> out) {
                try {
                    if (packet instanceof PacketPlayInUpdateSign) {
                        PacketPlayInUpdateSign updateSign = (PacketPlayInUpdateSign) packet;
                        if (updateSign.b().equals(pos)) {
                            String[] newLines = function.apply(player, updateSign.c());
                            if (newLines != null) {
                                IChatBaseComponent[] sanitizedLines = CraftSign.sanitizeLines(Arrays.copyOf(newLines, 4));
                                for (int i = 0; i < sanitizedLines.length; i++)
                                    sign.a(i, sanitizedLines[i]);
                                conn.sendPacket(sign.getUpdatePacket());
                                conn.sendPacket(new PacketPlayOutOpenSignEditor(pos));
                            } else {
                                pipeline.remove("SignGUI");
                                conn.sendPacket(new PacketPlayOutBlockChange(pos, ((CraftWorld) loc.getWorld()).getHandle().getType(pos)));
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

    private IBlockData getBlockData(Material type) {
        switch(type) {
            case OAK_SIGN:
                return Blocks.OAK_SIGN.getBlockData();
            case BIRCH_SIGN:
                return Blocks.BIRCH_SIGN.getBlockData();
            case SPRUCE_SIGN:
                return Blocks.SPRUCE_SIGN.getBlockData();
            case JUNGLE_SIGN:
                return Blocks.JUNGLE_SIGN.getBlockData();
            case ACACIA_SIGN:
                return Blocks.ACACIA_SIGN.getBlockData();
            case DARK_OAK_SIGN:
                return Blocks.DARK_OAK_SIGN.getBlockData();
            case CRIMSON_SIGN:
                return Blocks.CRIMSON_SIGN.getBlockData();
            case WARPED_SIGN:
                return Blocks.WARPED_SIGN.getBlockData();
            default:
                throw new IllegalArgumentException("No sign type");
        }
    }
}
