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

public class MojangWrapper1_20_R4 implements AdventureVersionWrapper {

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

    private net.kyori.adventure.text.Component[] adventureLines = null;

    @Override
    public void setAdventureLines(Object[] lines) {
        var tmp = new net.kyori.adventure.text.Component[4];
        for (int i = 0; i < lines.length; i++) {
            var line = lines[i];
            if (line instanceof net.kyori.adventure.text.Component comp) {
                tmp[i] = comp;
            } else if (line == null) {
                tmp[i] = null;
            } else {
                throw new IllegalArgumentException("line at index "+i+" is not net.kyori.adventure.text.Component");
            }
        }
        adventureLines = tmp;
    }

    private Component[] createLines(String[] textLines){
        var ret = new Component[4];
        if (adventureLines != null) {
            for (int i = 0; i < adventureLines.length; i++) {
                var comp = adventureLines[i];
                if (comp == null) {
                    ret[i] = Component.empty();
                } else {
                    ret[i] = new AdventureComponent(comp);
                }
            }
        } else {
            for (int i = 0; i < textLines.length; i++) {
                var line = textLines[i];
                ret[i] = Component.nullToEmpty(line);
            }
        }
        return ret;
    }

    @Override
    public void openSignEditor(Player player, String[] lines, Material type, DyeColor color, Location signLoc, BiConsumer<SignEditor, String[]> onFinish) throws IllegalAccessException {
        ServerPlayer p = ((CraftPlayer) player).getHandle();
        ServerGamePacketListenerImpl conn = p.connection;

        Location loc = signLoc != null ? signLoc : getDefaultLocation(player);
        BlockPos pos = new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        SignBlockEntity sign = new SignBlockEntity(pos, null);
        SignText signText = sign.getText(true) // flag = front/back of sign
                .setColor(net.minecraft.world.item.DyeColor.valueOf(color.toString()));
        var linesToSet = createLines(lines);
        for (int i = 0; i < linesToSet.length; i++)
            signText = signText.setMessage(i, linesToSet[i]);
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
    public void displayNewLines(Player player, SignEditor signEditor, String[] lines) {
        SignBlockEntity sign = (SignBlockEntity)signEditor.getSign();

        SignText newSignText = sign.getText(true);
        for(int i = 0; i < lines.length; ++i) {
            newSignText = newSignText.setMessage(i, Component.nullToEmpty(lines[i]));
        }
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
