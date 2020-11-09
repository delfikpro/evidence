package evidence.render;

public class Vec3d {

	public double x, y, z;

	public Vec3d() {
	}

	public Vec3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public String toString() {
		return "Vec3d{" + x + ", " + y + ", " + z + '}';
	}

	public double getY() {
		return y;
	}

	public Vi toVec3i() {
		return new Vi((int) x, (int) y, (int) z);
	}

}
