package evidence;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Font {

	static final byte[] glyphs = new byte[65536];
	static {try {initGlyphs();} catch (Exception ignored) {}}
	private static Image bint;

	public static void bind(Image image) {
		bint = image;
	}

	public static void drawStringWithShadow(String string, int x, int y) throws IOException {
		drawString(string, x + 1, y + 1, 4);
		drawString(string, x, y);
	}

	public static void drawString(String s, int x, int y) throws IOException {
		drawString(s, x, y, 1);
	}
	public static void drawString(String s, int x, int y, int colorFactor) throws IOException {

		boolean coloring = false;
		Color color = Color.WHITE;
		bint.setColor(1, 1, 1, 1);
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
					if (color == Color.GOLD && colorFactor != 1) color = Color.SHADOW_GOLD;
					bint.setColor(color, 1);
					float f = 1f / colorFactor;
					bint.permuteColor(f, f, f, 1);
					continue;
				}
			}

			int dx = x;
			if (bold) drawChar(c, x++, y, color, colorFactor);
			x += drawChar(c, x, y, color, colorFactor);
			if (underline) {
				int o = colorFactor == 1 ? 0 : 1;
				int rgb = new java.awt.Color(color.r / colorFactor, color.g / colorFactor, color.b / colorFactor).getRGB();
				bint.rect(dx - 2 + o, y + 16 + o, dx - 2 + o + 10, y + 16 + o + 2, rgb);
			}
		}
		bint.setColor(1, 1, 1, 1);
	}

	public static int drawChar(char c, float x, float y, Color color, int colorFactor) throws IOException {

		if (c == 32) return 4;

		BufferedImage image = getPage(c / 256);
		byte glyph = glyphs[c];
		c %= 256;
		int row = c / 16 * 16;
		int offset = glyph >>> 4 & 0b1111;
		int col = c % 16 * 16 + offset;
		int width = (glyph & 15) + 1 - offset;
		// 1 / 256
		double part = 0X1P-8;
		bint.draw(image.getRaster(), col * part, row * part, (col + width) * part, (row + 16) * part, x, y, x + width,y + 16, 1);
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
		if (c == 32) return 4;

		if (glyphs[c] != 0) {
			int rightOffset = glyphs[c] >>> 4;
			int width = glyphs[c] & 15;
			rightOffset = rightOffset & 15;
			++width;
			return width - rightOffset + 2;
		}
		return 0;
	}

}
