package de.rapha149.signgui.version;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class Wrapper1_14_R1 implements VersionWrapper {

    private final boolean IS_1_14_1 = Pattern.compile("1\\.14\\.\\d").matcher(Bukkit.getBukkitVersion()).find();

    /**
     * {@inheritDoc}
     */
    @Override
    public void openSignEditor(Player player, String[] lines, Material type, DyeColor color, BiFunction<Player, String[], String[]> function) throws NoSuchFieldException, IllegalAccessException {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        PlayerConnection conn = p.playerConnection;
        Location loc = getLocation(player);
        WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
        BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        TileEntitySign sign = new TileEntitySign();
        sign.setPosition(pos);

        Field field = sign.getClass().getDeclaredField(IS_1_14_1 ? "color" : "l");
        field.setAccessible(true);
        field.set(sign, EnumColor.valueOf(color.toString()));
        field.setAccessible(false);

        IChatBaseComponent[] sanitizedLines = CraftSign.sanitizeLines(lines);
        for (int i = 0; i < sanitizedLines.length; i++)
            sign.a(i, sanitizedLines[i]);

        /*PacketPlayOutBlockChange blockChange = new PacketPlayOutBlockChange(world, pos);
        blockChange.block = getBlockData(type);
        conn.sendPacket(blockChange);*/
        player.sendBlockChange(loc, type.createBlockData());
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
                                conn.sendPacket(new PacketPlayOutBlockChange(world, pos));
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
