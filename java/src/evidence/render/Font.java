package evidence.render;

import evidence.resources.ResourcePack;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Font {

	static final byte[] glyphs = new byte[65536];
	static {try {initGlyphs();} catch (Exception ignored) {}}
	private static ScaledImage bint;

	public static void bind(ScaledImage image) {
		bint = image;
	}

	public static void drawStringWithShadow(String string, float x, float y) throws IOException {
		drawString(string, x + 0.5F, y + 0.5F, 4);
		drawString(string, x, y);
	}

	public static void drawString(String s, float x, float y) throws IOException {
		drawString(s, x, y, 1);
	}
	public static void drawString(String s, float x, float y, int colorFactor) throws IOException {

		boolean coloring = false;
		Color color = Color.WHITE;
		color(color, colorFactor);
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
					color(color, colorFactor);
					continue;
				}
			}

			float dx = x;
			if (bold) {
				drawChar(c, x, y, color, colorFactor);
				x += 0.5f;
			}
			x += drawChar(c, x, y, color, colorFactor);
			if (underline) {
				float o = colorFactor == 1 ? 0 : 0.5f;
//				int rgb = new java.awt.Color(color.r, color.g, color.b).getRGB();
				bint.rect(dx - 1 + o, y + 8 + o, dx - 1 + o + 5, y + 8 + o + 1, -1);
			}
		}
		bint.setColor(1, 1, 1, 1);
	}

	private static void color(Color color, int factor) {
		if (color == Color.GOLD && factor != 1) color = Color.SHADOW_GOLD;
		bint.setColor(color, 1);
		float f = 1f / factor;
		bint.permuteColor(f, f, f, 1);
	}

	public static float drawChar(char c, float x, float y, Color color, int colorFactor) throws IOException {

		if (c == 32) return 4;

		BufferedImage image = getPage(c / 256);
		byte glyph = glyphs[c];
		c %= 256;
		int row = c / 16 * 16;
		int offset = glyph >>> 4 & 0b1111;
		int col = c % 16 * 16 + offset;
		float width = ((glyph & 15) + 1 - offset) / 2f;
		// 1 / 256
		double part = 0X1P-8;
		bint.draw(image, col * part, row * part, (col + width * 2) * part, (row + 16) * part, x, y, x + width,y + 8);
		return width + 1;

	}

	private static final BufferedImage[] pages = new BufferedImage[256];

	private static BufferedImage getPage(int page) throws IOException {
		if (pages[page] != null) return pages[page];
		String name = String.format("assets/minecraft/textures/font/unicode_page_%02x.png", page);
		return pages[page] = ResourcePack.getTexture(name);
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
