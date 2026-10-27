package com.dfsek.terra.addons.image.image;

import java.awt.image.BufferedImage;


public class BufferedImageWrapper implements Image {

    // The pixels as default-RGB ints, read once: BufferedImage.getRGB converts through the image's ColorModel on every
    // call, which made reading the map images a hot spot of chunk generation. The ints are what getRGB returns.
    private final int[] pixels;
    private final int width;
    private final int height;

    public BufferedImageWrapper(BufferedImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.pixels = image.getRGB(0, 0, width, height, null, 0, width);
    }

    @Override
    public int getRGB(int x, int y) {
        if(x < 0 || y < 0 || x >= width || y >= height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!"); // as BufferedImage.getRGB throws
        }
        return pixels[y * width + x];
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
