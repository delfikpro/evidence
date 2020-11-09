package evidence.hand;

import lombok.Data;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;

public class TextureMap {

	@Data
	public static class V2 {

		private final int x;
		private final int y;

	}

	@Data
	public static class Triplet {

		private final int red;
		private final int green;
		private final int blue;

		public int diff(int r, int g, int b) {
			return abs(red - r) + abs(green - g) + abs(blue - b);
		}

	}

	@SuppressWarnings ("PointlessArithmeticExpression")
	public static void doMap(BufferedImage src, BufferedImage srcMap, BufferedImage dst, BufferedImage dstMap) {

		int srcWidth = srcMap.getWidth();
		int srcHeight = srcMap.getHeight();

		Map<Triplet, V2> srcVariants = new HashMap<>();

		WritableRaster srcMapRaster = srcMap.getRaster();
		int srcFormat = srcMapRaster.getNumBands();
		int[] srcMapData = new int[srcFormat * srcHeight * srcWidth];
		srcMapRaster.getPixels(0, 0, srcWidth, srcHeight, srcMapData);

		for (int x = 0; x < srcWidth; x++) {
			for (int y = 0; y < srcHeight; y++) {
				int offsetImg = (y * srcWidth + x) * srcFormat;
				int a = srcMapData[offsetImg + 3];
				if (a < 255) continue;
				int r = srcMapData[offsetImg + 0];
				int g = srcMapData[offsetImg + 1];
				int b = srcMapData[offsetImg + 2];
				srcVariants.put(new Triplet(r, g, b), new V2(x, y));
			}
		}

		int dstWidth = dstMap.getWidth();
		int dstHeight = dstMap.getHeight();

		WritableRaster dstMapRaster = dstMap.getRaster();
		int dstFormat = dstMapRaster.getNumBands();
		int[] dstMapData = new int[dstFormat * dstHeight * dstWidth];
		dstMapRaster.getPixels(0, 0, dstWidth, dstHeight, dstMapData);

		for (int x = 0; x < dstWidth; x++) {
			for (int y = 0; y < dstHeight; y++) {
				int offsetImg = (y * dstWidth + x) * dstFormat;
				int a = dstMapData[offsetImg + 3];
				if (a < 255) continue;
				int r = dstMapData[offsetImg + 0];
				int g = dstMapData[offsetImg + 1];
				int b = dstMapData[offsetImg + 2];

				int minDiff = Integer.MAX_VALUE;
				V2 coords = null;

				for (Map.Entry<Triplet, V2> entry : srcVariants.entrySet()) {
					int diff = entry.getKey().diff(r, g, b);
					if (diff >= minDiff) continue;
					minDiff = diff;
					coords = entry.getValue();
				}

				if (coords == null)
					throw new IllegalArgumentException("Couldn't find any color for " + r + " " + g + " " + b + " (" + x + ", " + y + ")");

				int rgb = src.getRGB(coords.x, coords.y);
				if (rgb >>> 24 > 0) dst.setRGB(x, y, rgb);
//					dst.setRGB(x, y, 0xFF000000 | ((int) (coords.x / 64.0 * 255)) << 16 | ((int) (coords.y / 64.0 * 255)));
//				dst.setRGB(x, y, 2 == 0 ? a << 24 | b << 16 | g << 8 | r : (src.getRGB(coords.x, coords.y) | 0xFF000000));
			}
		}


	}

}
