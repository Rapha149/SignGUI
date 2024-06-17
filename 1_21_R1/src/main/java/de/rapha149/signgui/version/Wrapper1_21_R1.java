package de.rapha149.signgui.version;

import de.rapha149.signgui.SignEditor;
import de.rapha149.signgui.SignGUIChannelHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayInUpdateSign;
import net.minecraft.network.protocol.game.PacketPlayOutOpenSignEditor;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.TileEntitySign;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class Wrapper1_21_R1 implements VersionWrapper {

    private final Field NETWORK_MANAGER_FIELD;

    {
        Field networkManagerField = null;
        for (Field field : ServerCommonPacketListenerImpl.class.getDeclaredFields()) {
            if (field.getType() == NetworkManager.class) {
                field.setAccessible(true);
                networkManagerField = field;
                break;
            }
        }

        NETWORK_MANAGER_FIELD = networkManagerField;
    }

    @Override
    public Material getDefaultType() {
        return Material.OAK_SIGN;
    }

    @Override
    public List<Material> getSignTypes() {
        return Arrays.asList(Material.OAK_SIGN, Material.BIRCH_SIGN, Material.SPRUCE_SIGN, Material.JUNGLE_SIGN,
                Material.ACACIA_SIGN, Material.DARK_OAK_SIGN, Material.CRIMSON_SIGN, Material.WARPED_SIGN,
                Material.CHERRY_SIGN, Material.MANGROVE_SIGN, Material.BAMBOO_SIGN
        );
    }

    @Override
    public void openSignEditor(Player player, String[] lines, Material type, DyeColor color, Location signLoc, BiConsumer<SignEditor, String[]> onFinish) throws IllegalAccessException {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        PlayerConnection conn = p.c;

        if (NETWORK_MANAGER_FIELD == null)
            throw new IllegalStateException("Unable to find NetworkManager field in PlayerConnection class.");
        if (!NETWORK_MANAGER_FIELD.canAccess(conn)) {
            NETWORK_MANAGER_FIELD.setAccessible(true);
            if (!NETWORK_MANAGER_FIELD.canAccess(conn))
                throw new IllegalStateException("Unable to access NetworkManager field in PlayerConnection class.");
        }

        Location loc = signLoc != null ? signLoc : getDefaultLocation(player);
        BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        TileEntitySign sign = new TileEntitySign(pos, null);
        SignText signText = sign.a(true) // flag = front/back of sign
                .a(EnumColor.valueOf(color.toString()));
        for (int i = 0; i < lines.length; i++)
            signText = signText.a(i, IChatBaseComponent.a(lines[i]));
        sign.a(signText, true);

        boolean schedule = false;
        NetworkManager manager = (NetworkManager) NETWORK_MANAGER_FIELD.get(conn);
        ChannelPipeline pipeline = manager.n.pipeline();
        if (pipeline.names().contains("SignGUI")) {
            ChannelHandler handler = pipeline.get("SignGUI");
            if (handler instanceof SignGUIChannelHandler<?> signGUIHandler) {
                signGUIHandler.close();
                schedule = signGUIHandler.getBlockPosition().equals(pos);
            }

            if (pipeline.names().contains("SignGUI"))
                pipeline.remove("SignGUI");
        }

        Runnable runnable = () -> {
            player.sendBlockChange(loc, type.createBlockData());
            sign.a(p.dO());
            conn.b(sign.l());
            sign.a((World) null);
            conn.b(new PacketPlayOutOpenSignEditor(pos, true)); // flag = front/back of sign

            SignEditor signEditor = new SignEditor(sign, loc, pos, pipeline);
            pipeline.addAfter("decoder", "SignGUI", new SignGUIChannelHandler<Packet<?>>() {

                @Override
                public Object getBlockPosition() {
                    return pos;
                }

                @Override
                public void close() {
                    closeSignEditor(player, signEditor);
                }

                @Override
                protected void decode(ChannelHandlerContext chc, Packet<?> packet, List<Object> out) {
                    try {
                        if (packet instanceof PacketPlayInUpdateSign updateSign) {
                            if (updateSign.b().equals(pos))
                                onFinish.accept(signEditor, updateSign.f());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    out.add(packet);
                }
            });
        };

        if (schedule)
            SCHEDULER.schedule(runnable, 200, TimeUnit.MILLISECONDS);
        else
            runnable.run();
    }

    @Override
    public void displayNewLines(Player player, SignEditor signEditor, String[] lines) {
        TileEntitySign sign = (TileEntitySign) signEditor.getSign();

        SignText newSignText = sign.a(true);
        for (int i = 0; i < lines.length; i++)
            newSignText = newSignText.a(i, IChatBaseComponent.a(lines[i]));
        sign.a(newSignText, true);

        EntityPlayer p = ((CraftPlayer) player).getHandle();
        PlayerConnection conn = p.c;
        sign.a(p.dO());
        conn.b(sign.l());
        sign.a((World) null);
        conn.b(new PacketPlayOutOpenSignEditor((BlockPosition) signEditor.getBlockPosition(), true));
    }

    @Override
    public void closeSignEditor(Player player, SignEditor signEditor) {
        Location loc = signEditor.getLocation();
        signEditor.getPipeline().remove("SignGUI");
        player.sendBlockChange(loc, loc.getBlock().getBlockData());
    }
}
