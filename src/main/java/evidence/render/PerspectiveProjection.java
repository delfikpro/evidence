package evidence.render;


import static java.lang.Math.*;

public class PerspectiveProjection {

	public final double f, a, zNear, zFar, q;

	public PerspectiveProjection(double fov, double aspectRatio, double zNear, double zFar) {
		this.f = 1 / tan(toRadians(fov / 2));
		this.a = aspectRatio;
		this.zNear = zNear;
		this.zFar = zFar;
		this.q = zFar / (zFar - zNear);
	}

	@SuppressWarnings("UnusedReturnValue")
	public Vec3d project(Vec3d p) {
		p.x = p.x * f * a / 2 / p.z;
		p.y = p.y * f / 2 / p.z;
		p.z = p.z * q - zNear * q;
		return p;
	}

	public static class Windowed extends PerspectiveProjection {

		public final double width, height;

		public Windowed(double fov, int width, int height, double zNear, double zFar) {
			super(fov, (double) width / (double) height, zNear, zFar);
			this.width = width / 2d;
			this.height = height / 2d;
		}

		@Override
		public Vec3d project(Vec3d p) {
			p.x = p.x / width - 1;
			p.y = p.y / height - 1;
			super.project(p);
			p.x = (p.x + 1) * width;
			p.y = (p.y + 1) * height;
			return p;
		}

	}


}
