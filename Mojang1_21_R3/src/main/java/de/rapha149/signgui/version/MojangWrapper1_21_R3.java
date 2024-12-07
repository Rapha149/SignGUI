package de.rapha149.signgui.version;

import de.rapha149.signgui.SignEditor;
import de.rapha149.signgui.SignGUIChannelHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.papermc.paper.adventure.AdventureComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class MojangWrapper1_21_R3 implements VersionWrapper {

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

    private static Component[] createLines(String[] textLines, Object[] adventureLines) {
        Component[] lines = new Component[4];
        if (adventureLines != null) {
            for (int i = 0; i < adventureLines.length; i++) {
                Object line = adventureLines[i];
                if (line instanceof net.kyori.adventure.text.Component component) {
                    lines[i] = new AdventureComponent(component);
                } else if (line == null) {
                    lines[i] = Component.empty();
                } else {
                    throw new IllegalArgumentException("line at index " + i + " is not net.kyori.adventure.text.Component");
                }
            }
        } else {
            for (int i = 0; i < textLines.length; i++)
                lines[i] = Component.nullToEmpty(textLines[i]);
        }
        return lines;
    }

    @Override
    public void openSignEditor(Player player, String[] textLines, Object[] adventureLines, Material type, DyeColor color, boolean glow, Location signLoc, BiConsumer<SignEditor, String[]> onFinish) {
        ServerPlayer p = ((CraftPlayer) player).getHandle();
        ServerGamePacketListenerImpl conn = p.connection;

        Location loc = signLoc != null ? signLoc : getDefaultLocation(player);
        BlockPos pos = new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        SignBlockEntity sign = new SignBlockEntity(pos, Blocks.OAK_SIGN.defaultBlockState());
        SignText signText = sign.getText(true) // flag = front/back of sign
                .setColor(net.minecraft.world.item.DyeColor.valueOf(color.toString()))
                .setHasGlowingText(glow);

        Component[] lines = createLines(textLines, adventureLines);
        for (int i = 0; i < lines.length; i++)
            signText = signText.setMessage(i, lines[i]);
        sign.setText(signText, true);

        boolean schedule = false;
        Connection manager = conn.connection;
        ChannelPipeline pipeline = manager.channel.pipeline();
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
            sign.setLevel(p.level());
            conn.send(sign.getUpdatePacket());
            sign.setLevel(null);
            conn.send(new ClientboundOpenSignEditorPacket(pos, true)); // flag = front/back of sign

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
                        if (packet instanceof ServerboundSignUpdatePacket updateSign) {
                            if (updateSign.getPos().equals(pos))
                                onFinish.accept(signEditor, updateSign.getLines());
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
    public void displayNewLines(Player player, SignEditor signEditor, String[] textLines, Object[] adventureLines) {
        SignBlockEntity sign = (SignBlockEntity)signEditor.getSign();

        SignText newSignText = sign.getText(true);
        Component[] lines = createLines(textLines, adventureLines);
        for(int i = 0; i < textLines.length; ++i)
            newSignText = newSignText.setMessage(i, lines[i]);
        sign.setText(newSignText, true);

        ServerPlayer p = ((CraftPlayer)player).getHandle();
        ServerGamePacketListenerImpl conn = p.connection;
        sign.setLevel(p.level());
        conn.send(sign.getUpdatePacket());
        sign.setLevel(null);
        conn.send(new ClientboundOpenSignEditorPacket((BlockPos)signEditor.getBlockPosition(), true));
    }

    @Override
    public void closeSignEditor(Player player, SignEditor signEditor) {
        Location loc = signEditor.getLocation();
        signEditor.getPipeline().remove("SignGUI");
        player.sendBlockChange(loc, loc.getBlock().getBlockData());
    }
}
