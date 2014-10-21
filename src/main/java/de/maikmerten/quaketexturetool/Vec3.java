package de.maikmerten.quaketexturetool;

/**
 *
 * @author maik
 */
public class Vec3 {

	private double x, y, z;

	public Vec3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getLength() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public void scale(double s) {
		x = x * s;
		y = y * s;
		z = z * s;
	}

	public void normalize() {
		scale(1.0 / getLength());
	}

	public double dot(Vec3 v) {
		return (this.x * v.x) + (this.y * v.y) + (this.z * v.z);
	}
	
	@Override
	public String toString() {
		return "Vec3: " + x + " " + y + " " + z;
	}

}
