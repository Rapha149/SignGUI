package io.github.rapha149.signgui;

import io.netty.channel.ChannelPipeline;
import org.bukkit.Location;

public class SignEditor {

    private final Object sign;
    private final Location location;
    private final Object blockPosition;
    private final ChannelPipeline pipeline;

    public SignEditor(Object sign, Location location, Object blockPosition, ChannelPipeline pipeline) {
        this.sign = sign;
        this.location = location;
        this.blockPosition = blockPosition;
        this.pipeline = pipeline;
    }

    public Object getSign() {
        return sign;
    }

    public Location getLocation() {
        return location;
    }

    public Object getBlockPosition() {
        return blockPosition;
    }

    public ChannelPipeline getPipeline() {
        return pipeline;
    }
}
