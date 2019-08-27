package evidence;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Screenshot {

	static final byte[] glyphs = new byte[65536];
	static {try {initGlyphs();} catch (Exception ignored) {}}

	private final int scale;
	private final BufferedImage image;
	private final Graphics2D g;

	public Screenshot(String path, int scale) throws IOException {
		this(new File(path), scale);
	}

	public Screenshot(File screenshotFile, int scale) throws IOException {
		image = ImageIO.read(screenshotFile);
		g = image.createGraphics();
		this.scale = scale;
	}

	public void drawStringWithShadow(String string, int x, int y) throws IOException {
		drawString(string, x + 1, y + 1, 4);
		drawString(string, x, y);
	}

	public void drawString(String s, int x, int y) throws IOException {
		drawString(s, x, y, 1);
	}
	public void drawString(String s, int x, int y, int colorFactor) throws IOException {

		boolean coloring = false;
		Color color = Color.WHITE;
		boolean bold = false;
		boolean underline = false;
		for (char c : s.toCharArray()) {
			if (c == '§') {
				coloring = true;
				continue;
			}
			if (coloring) {
				if (c == 'l') {
					bold = true;
					coloring = false;
					continue;
				} else if (c == 'n') {
					underline = true;
					coloring = false;
					continue;
				} else {
					underline = false;
					bold = false;
					int code = "0123456789abcdef".indexOf(c);
					coloring = false;
					color = code < 0 ? Color.WHITE : Color.values()[code];
					continue;
				}
			}

			int dx = x;
			if (bold) drawChar(c, x++, y, color, colorFactor);
			x += drawChar(c, x, y, color, colorFactor);
			if (underline) {
				int o = colorFactor == 1 ? 0 : 1;
				int rgb = new java.awt.Color(color.r / colorFactor, color.g / colorFactor, color.b / colorFactor).getRGB();
				drawRect(dx - 2 + o, y + 16 + o, 10, 2, rgb);
			}
		}
	}

	public int drawChar(char c, int x, int y, Color color, int colorFactor) throws IOException {

		if (c == 32) return 8;

		BufferedImage image = getPage(c / 256);
		byte glyph = glyphs[c];
		c %= 256;
		int row = c / 16 * 16;
		int offset = glyph >>> 4 & 0b1111;
		int col = c % 16 * 16 + offset;
		int width = (glyph & 15) + 1 - offset;
		if (color != null) {
			BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
			WritableRaster raster = img.getRaster();
			image.copyData(raster);
			image = ImageUtil.colorImage(img, color.r / colorFactor, color.g / colorFactor, color.b / colorFactor);
		}
		g.drawImage(image, x * scale, y * scale, (x + width) * scale, (y + 16) * scale, col, row, col + width, row + 16, null);
		return width + 2;

	}

	private static final BufferedImage[] pages = new BufferedImage[256];

	private static BufferedImage getPage(int page) throws IOException {
		if (pages[page] != null) return pages[page];
		String name = String.format("assets/minecraft/textures/font/unicode_page_%02x.png", page);
		BufferedImage one = ImageIO.read(ResourcePack.get(name));
		BufferedImage img = new BufferedImage(one.getWidth(), one.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.drawImage(one, 0, 0, 256, 256, 0, 0, 256, 256, null);
		g.dispose();
		return pages[page] = img;
	}

	private static void initGlyphs() throws IOException {
		InputStream is = ResourcePack.get("assets/minecraft/font/glyph_sizes.bin");
		is.read(glyphs);
		is.close();
	}


	private static int getStringWidth(String s) {
		int w = 0;
		for (char c : s.toCharArray()) w += getCharWidth(c);
		return w;
	}

	private static int getCharWidth(char c) {
		if (c == 167) {
			return -1;
		}
		if (c == 32) return 8;

		if (glyphs[c] != 0) {
			int rightOffset = glyphs[c] >>> 4;
			int width = glyphs[c] & 15;
			rightOffset = rightOffset & 15;
			++width;
			return width - rightOffset + 2;
		}
		return 0;
	}


	public int getHeight() {
		return image.getHeight() / scale;
	}
	public int getWidth() {
		return image.getWidth() / scale;
	}

	public void save(String path) throws IOException {
		ImageIO.write(image, "PNG", new File(path));
	}


	public void drawImageInversionMask(String s, int x, int y, int w, int h) throws IOException {

		BufferedImage i = ImageIO.read(ResourcePack.get(s));
		WritableRaster r = i.getRaster();
		WritableRaster t = image.getRaster();

		for (int xx = 0; xx < w; xx++) {
			for (int yy = 0; yy < h; yy++) {
				int[] from = r.getPixel(xx, yy, (int[]) null);

				if (from[0] != 1 || from[1] != 1 || from[2] != 1) continue;

				for (int j = 0; j < 4; j++) {
					int nx = xx * 2 + j / 2;
					int ny = yy * 2 + j % 2;
					int[] to = t.getPixel(x + nx, y + ny, (int[]) null);
					to[0] = 255 - to[0];
					to[1] = 255 - to[1];
					to[2] = 255 - to[2];
					t.setPixel(x + nx, y + ny, to);
				}
			}
		}

	}



	public void drawBlackRect(int x, int y, int w, int h) {

		WritableRaster t = image.getRaster();

		for (int xx = x; xx < x + w; xx++) {
			for (int yy = y; yy < y + h; yy++) {
				int[] px = t.getPixel(xx, yy, (int[]) null);
				px[0] /= 2;
				px[1] /= 2;
				px[2] /= 2;
				t.setPixel(xx, yy, px);
			}
		}

	}

	public void drawRect(int x1, int y1, int width, int height, int color) {
		g.setColor(new java.awt.Color(color));
		g.fillRect(x1, y1, width, height);
	}

	public void overlapImage(String path) throws IOException {
		BufferedImage i = ImageIO.read(new File(path));
		g.drawImage(i, 0, 0, image.getWidth(), image.getHeight(), 0, 0, i.getWidth(), i.getHeight(), null);
	}

	public Graphics2D getGraphics() {
		return g;
	}

	public void pasteImage(BufferedImage i, int srcX, int srcY, int w, int h, int dstX, int dstY) {
		g.drawImage(i, dstX, dstY, dstX + w, dstY + h, srcX, srcY, srcX + w, srcY + h, null);
	}
}
