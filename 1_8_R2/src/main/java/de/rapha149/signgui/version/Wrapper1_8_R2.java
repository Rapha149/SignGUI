package de.rapha149.signgui.version;

import de.rapha149.signgui.SignEditor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_8_R2.*;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

@SuppressWarnings("deprecation")
public class Wrapper1_8_R2 implements VersionWrapper {

    @Override
    public Material getDefaultType() {
        return Material.SIGN_POST;
    }

    @Override
    public List<Material> getSignTypes() {
        return Arrays.asList(Material.SIGN_POST);
    }

    @Override
    public void openSignEditor(Player player, String[] lines, Material type, DyeColor color, Location signLoc, BiConsumer<SignEditor, String[]> onFinish) {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        PlayerConnection conn = p.playerConnection;
        Location loc = signLoc != null ? signLoc : getDefaultLocation(player);
        BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        TileEntitySign sign = new TileEntitySign();
        sign.a(pos);
        for (int i = 0; i < lines.length; i++)
            sign.lines[i] = new ChatComponentText(lines[i] != null ? lines[i] : "");

        player.sendBlockChange(loc, type, (byte) 0);
        conn.sendPacket(sign.getUpdatePacket());
        conn.sendPacket(new PacketPlayOutOpenSignEditor(pos));

        ChannelPipeline pipeline = conn.networkManager.k.pipeline();
        if (pipeline.names().contains("SignGUI"))
            pipeline.remove("SignGUI");

        SignEditor signEditor = new SignEditor(sign, loc, pos, pipeline);
        pipeline.addAfter("decoder", "SignGUI", new MessageToMessageDecoder<Packet<?>>() {
            @Override
            protected void decode(ChannelHandlerContext chc, Packet<?> packet, List<Object> out) {
                try {
                    if (packet instanceof PacketPlayInUpdateSign) {
                        PacketPlayInUpdateSign updateSign = (PacketPlayInUpdateSign) packet;
                        if (updateSign.a().equals(pos))
                            onFinish.accept(signEditor, Arrays.stream(updateSign.b()).map(IChatBaseComponent::getText).toArray(String[]::new));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                out.add(packet);
            }
        });
    }

    @Override
    public void displayNewLines(Player player, SignEditor signEditor, String[] lines) {
        TileEntitySign sign = (TileEntitySign) signEditor.getSign();
        for (int i = 0; i < lines.length; i++)
            sign.lines[i] = new ChatComponentText(lines[i] != null ? lines[i] : "");

        PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
        conn.sendPacket(sign.getUpdatePacket());
        conn.sendPacket(new PacketPlayOutOpenSignEditor((BlockPosition) signEditor.getBlockPosition()));
    }

    @Override
    public void closeSignEditor(Player player, SignEditor signEditor) {
        Location loc = signEditor.getLocation();
        signEditor.getPipeline().remove("SignGUI");
        player.sendBlockChange(loc, loc.getBlock().getType(), loc.getBlock().getData());
    }
}
