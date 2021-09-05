package com.loucaskreger.placementpreview.renderer;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper for VertexConsumerProvider that provides the color tints to the {@link VertexConsumerWrapper#color(int, int, int, int)}
 */
public class VertexConsumerProviderWrapper implements VertexConsumerProvider {
    private final VertexConsumerProvider inner;
    private final int red, green, blue, alpha;
    private final boolean overrideRenderLayer;
    private final RenderLayer renderLayer;



    public VertexConsumerProviderWrapper(VertexConsumerProvider inner, int red, int green, int blue, int alpha, boolean overrideRenderLayer, @Nullable RenderLayer layer) {
        this.inner = inner;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.overrideRenderLayer = overrideRenderLayer;
        this.renderLayer = layer;
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        return new VertexConsumerWrapper(inner.getBuffer(overrideRenderLayer ? this.renderLayer : layer), red, green, blue, alpha);
    }


}
