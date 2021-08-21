package com.loucaskreger.placementpreview.renderer;

import net.minecraft.client.render.VertexConsumer;

/**
 * Wrapper for VertexConsumer, allows passing in tints in order to alter the color and opacity.
 */
public class VertexConsumerWrapper implements VertexConsumer {

    private final VertexConsumer inner;
    private final int redTint, greenTint, blueTint, alphaTint;

    public VertexConsumerWrapper(VertexConsumer inner, int redTint, int greenTint, int blueTint, int alphaTint) {
        this.inner = inner;
        this.redTint = redTint;
        this.greenTint = greenTint;
        this.blueTint = blueTint;
        this.alphaTint = alphaTint;
    }


    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        return inner.vertex(x, y, z);
    }


    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        // Modify the color that gets passed to the consumer
        return inner.color((red * redTint) / 255, (green * greenTint) / 255, (blue * blueTint) / 255, (alpha * alphaTint) / 255);
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return inner.texture(u, v);
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return inner.overlay(u, v);
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return inner.light(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return inner.normal(x, y, z);
    }

    @Override
    public void next() {
        inner.next();
    }

    @Override
    public void fixedColor(int red, int green, int blue, int alpha) {
        inner.fixedColor(red, green, blue, alpha);
    }

    @Override
    public void unfixColor() {
        inner.unfixColor();
    }
}
