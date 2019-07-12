package evidence;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class ImageUtil {

	public static BufferedImage colorImage(BufferedImage image, int r, int g, int b) {
		int width = image.getWidth();
		int height = image.getHeight();
		WritableRaster raster = image.getRaster();

		for (int xx = 0; xx < width; xx++) {
			for (int yy = 0; yy < height; yy++) {
				int[] pixels = raster.getPixel(xx, yy, (int[]) null);
				pixels[0] = r;
				pixels[1] = g;
				pixels[2] = b;
				raster.setPixel(xx, yy, pixels);
			}
		}
		return image;
	}

}
