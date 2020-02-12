package evidence;

import evidence.render.*;
import evidence.render.deep.CGI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class PerspectiveTest {

	public static void main(String[] args) throws IOException {
		int dim = 1024;
		ScaledImage img = new ScaledImage(new BufferedImage(dim, dim, BufferedImage.TYPE_4BYTE_ABGR), 1);
		System.out.println(img);
		PerspectiveProjection.Windowed proj = new PerspectiveProjection.Windowed(45, dim, dim, 0.125, 128);

		BufferedImage texture = Util.convertColorspace(ImageIO.read(new File("lava.png")), BufferedImage.TYPE_4BYTE_ABGR);

		Vi a = p(proj, 100, 100, 10).toVec3i();
		Vi b = p(proj, 100, 900, 10).toVec3i();
		Vi c = p(proj, 900, 100, 10).toVec3i();
		c.u = 1;
		b.v = 1;

		Vi[] s = {a, b, c};
		Arrays.sort(s, Comparator.comparingInt(v -> v.y));
		a = s[0];
		b = s[1];
		c = s[2];

		System.out.println("texture dims: " + texture.getWidth() + " x " + texture.getHeight());

		long time = System.currentTimeMillis();

		CGI cgi = new CGI(dim, dim);
		cgi.drawTriangle(texture, img, a, b, c);

		for (int l = a.y; l < b.y; l++) {
			step(l, a, b, c, img, a, b, (l - a.y) / (float) (b.y - a.y), a, texture);
		}
		for (int l = b.y; l <= c.y; l++) {
			step(l, a, b, c, img, b, c, (b.y - l) / (float) (c.y - b.y), c, texture);
		}

		System.out.println(a);
		System.out.println(b);
		System.out.println(c);

		img.rect(0, 0, 1, dim, -1);
		img.rect(0, 0, dim, 1, -1);
		img.rect(dim - 1, 0, dim, dim, -1);
		img.rect(0, dim - 1, dim, dim, -1);

		Vi[] arr = {a, b, c};
		for (Vi v : arr) {
			img.rect(v.x, v.y, v.x + 1, v.y + 1, -1);
		}
		img.flush();
		img.save(new File("test.png"));

		System.out.println(System.currentTimeMillis() - time + " ms.");
	}

	public static int lin(int a, int b, float v) {
		return (int) ((b - a) * v) + a;
	}

	static Vec3d p(PerspectiveProjection proj, float x, float y, float z) {
		return proj.project(new Vec3d(x, y, z));
	}

	static void step(int l, Vi a, Vi b, Vi c, ScaledImage img, Vi entry, Vi limit, float slide, Vi slidebase, BufferedImage texture) {
		float fall = (l - a.y) / (float) (c.y - a.y);
		int slideX = (int) (slide * (b.x - slidebase.x ));
		int fallX = (int) (fall * (c.x - a.x ));
//		int fromX = slide <= fall ? slideX : fallX;
//		int toX = slide >= fall ? slideX : fallX;
		int fromX = Math.min(slideX, fallX);
		int toX = Math.max(slideX, fallX);

		float slideU = (float) (Math.abs(slide) * (limit.u - entry.u) + entry.u);
		float slideV = (float) (Math.abs(slide) * (limit.v - entry.v) + entry.v);
		float fallU = (float) (fall * (c.u - a.u) + a.u);
		float fallV = (float) (fall * (c.v - a.v) + a.v);
		int lower = fromX + (fromX == slideX ? entry.x : a.x);
		int upper = toX + (fromX == slideX ? a.x : entry.x);

		if (lower > upper) {
			int temp = lower;
			lower = upper;
			upper = temp;
		}

//		boolean flag = fromX == slideX;
//		int lower = fromX + (flag ? entry.x : a.x);
//		int upper = toX + (flag ? a.x : entry.x);
		System.out.printf("Step number %d: fall is %.2f (%dpx), slide is %.2f (%dpx) | lower %d, upper %d\n", l, fall, fallX, slide, slideX, lower, upper);
		System.out.printf("SlideU: %.3f, SlideV: %.3f, FallU: %.3f, FallV: %.3f\n", slideU, slideV, fallU, fallV);
		boolean inverted = b.x < c.x;
		for (int x = lower; x < upper; x++) {
			float xd = (float) (x - lower) / (upper - lower);
			if (inverted) xd = 1 - xd;
			float u = (fallU) + xd * (slideU - fallU);
			float v = (fallV) + xd * (slideV - fallV);
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
