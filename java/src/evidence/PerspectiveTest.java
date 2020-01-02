package evidence;

import evidence.render.PerspectiveProjection;
import evidence.render.ScaledImage;
import evidence.render.Vec3d;

import java.awt.image.BufferedImage;

public class PerspectiveTest {

	public static void main(String[] args) {
		ScaledImage img = new ScaledImage(new BufferedImage(500, 500, BufferedImage.TYPE_3BYTE_BGR), 1);
		PerspectiveProjection.Windowed proj = new PerspectiveProjection.Windowed(90, 500, 500, 0.125, 128);

		Vec3d a = p(proj, 20, 20, 20);
		Vec3d b = p(proj, 40, 30, 50);
		Vec3d c = p(proj, 10, 50, 40);

//		img.line(a, b, 1, -1);
//		img.line(b, c, 1, -1);
//		img.line(c, a, 1, -1);
	}

	static Vec3d p(PerspectiveProjection proj, double x, double y, double z) {
		return proj.project(new Vec3d(x, y, z));
	}

}
