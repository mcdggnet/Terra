package com.dfsek.terra.addons.image.image;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class BufferedImageWrapperTest {
    private static BufferedImage random(int type, long seed) {
        BufferedImage image = new BufferedImage(37, 23, type);
        Random random = new Random(seed);
        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) image.setRGB(x, y, random.nextInt());
        }
        return image;
    }

    /** As packs load them: a PNG read back through ImageIO. */
    private static BufferedImage png(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
    }

    private static void assertSamePixels(BufferedImage image) {
        BufferedImageWrapper wrapper = new BufferedImageWrapper(image);
        assertEquals(image.getWidth(), wrapper.getWidth());
        assertEquals(image.getHeight(), wrapper.getHeight());
        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                assertEquals(image.getRGB(x, y), wrapper.getRGB(x, y), "type " + image.getType() + " at " + x + ", " + y);
            }
        }
    }

    @Test
    public void pixelsMatchBufferedImage() throws IOException {
        int[] types = {
            BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR,
            BufferedImage.TYPE_BYTE_GRAY, BufferedImage.TYPE_USHORT_GRAY, BufferedImage.TYPE_BYTE_INDEXED, BufferedImage.TYPE_INT_ARGB_PRE
        };
        for(int type : types) {
            BufferedImage image = random(type, type);
            assertSamePixels(image);
            assertSamePixels(png(image));
        }
    }

    @Test
    public void outsideTheImageThrowsAsBufferedImageDoes() {
        BufferedImageWrapper wrapper = new BufferedImageWrapper(random(BufferedImage.TYPE_INT_ARGB, 1));
        for(int[] at : new int[][]{ { -1, 0 }, { 0, -1 }, { 37, 0 }, { 0, 23 } }) {
            assertThrows(ArrayIndexOutOfBoundsException.class, () -> wrapper.getRGB(at[0], at[1]));
        }
    }
}
