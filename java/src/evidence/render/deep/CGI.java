package evidence.render.deep;

import evidence.render.ScaledImage;
import evidence.render.Vi;

import java.awt.image.BufferedImage;

public class CGI {

	private final byte[] DEPTH_BUFFER;
	private final byte[] COlOR_BUFFER;
	private final int width;
	private final int height;

	public CGI(int width, int height) {
		this.width = width;
		this.height = height;
		DEPTH_BUFFER = new byte[width * height];
		COlOR_BUFFER = new byte[width * height * 4];
	}

	public void drawTriangle(BufferedImage texture, ScaledImage dst, Vi... s) {
		Vi a, b, c;
		if (s[0].y > s[1].y) {
			if (s[0].y > s[2].y) {
				a = s[0];
				if (s[1].y > s[2].y) {
					b = s[1];
					c = s[2];
				} else {
					b = s[2];
					c = s[1];
				}
			} else {
				b = s[0];
				a = s[2];
				c = s[1];
			}
		} else {
			if (s[1].y > s[2].y) {
				a = s[1];
				if (s[0].y > s[2].y) {
					b = s[0];
					c = s[2];
				} else {
					b = s[2];
					c = s[0];
				}
			} else {
				b = s[1];
				a = s[2];
				c = s[0];
			}
		}

		for (int l = a.y; l < b.y; l++) {
			step(l, a, b, c, dst, a, b, (l - a.y) / (float) (b.y - a.y), a, texture);
		}
		for (int l = b.y; l <= c.y; l++) {
			step(l, a, b, c, dst, b, c, (b.y - l) / (float) (c.y - b.y), c, texture);
		}

	}


	static void step(int l, Vi a, Vi b, Vi c, ScaledImage img, Vi entry, Vi limit, float slide, Vi slidebase, BufferedImage texture) {
		float fall = (l - a.y) / (float) (c.y - a.y);
		int slideX = (int) (slide * (b.x - slidebase.x));
		int fallX = (int) (fall * (c.x - a.x));
		int fromX = Math.min(slideX, fallX);
		int toX = Math.max(slideX, fallX);

		double slideU = Math.abs(slide) * (limit.u - entry.u) + entry.u;
		double slideV = Math.abs(slide) * (limit.v - entry.v) + entry.v;
		double fallU = fall * (c.u - a.u) + a.u;
		double fallV = fall * (c.v - a.v) + a.v;
		int lower = fromX + (fromX == slideX ? entry.x : a.x);
		int upper = toX + (fromX == slideX ? a.x : entry.x);

		if (lower > upper) {
			int temp = lower;
			lower = upper;
			upper = temp;
		}

		System.out.printf("Step number %d: fall is %.2f (%dpx), slide is %.2f (%dpx) | lower %d, upper %d\n", l, fall, fallX, slide, slideX, lower, upper);
		System.out.printf("SlideU: %.3f, SlideV: %.3f, FallU: %.3f, FallV: %.3f\n", slideU, slideV, fallU, fallV);
		boolean inverted = b.x < c.x;
		for (int x = lower; x < upper; x++) {
			float xd = (float) (x - lower) / (upper - lower);
			if (inverted) xd = 1 - xd;
			double u = (fallU) + xd * (slideU - fallU);
			double v = (fallV) + xd * (slideV - fallV);
			if (u > 1) u = 1;
			if (v > 1) v = 1;
			if (u < 0) u = 0;
			if (v < 0) v = 0;

			int tx = (int) ((texture.getWidth()) * u);
			int ty = (int) ((texture.getHeight()) * v);
			if (tx >= texture.getWidth()) tx = texture.getWidth() - 1;
			if (ty >= texture.getHeight()) ty = texture.getHeight() - 1;
			int color;
			try {
				color = texture.getRGB(tx, ty);
			} catch (ArrayIndexOutOfBoundsException ex) {
				System.out.println("err " + tx + ", " + ty);
				throw ex;
			}

			img.rect(x, l, x + 1, l + 1,
					//					0xff005555 | (int) (u * 255) << 16
					color
					);
		}
		int invmask = inverted ? 0 : 8;
		img.rect(slideX + entry.x, l, slideX + 1 + entry.x, l + 1, (int) (slideU * 255) << invmask | 0xFF000000);
		img.rect(fallX + a.x, l, fallX + 1 + a.x, l + 1, (int) (fallU * 255) << invmask | 0xFF000000);
	}


}
