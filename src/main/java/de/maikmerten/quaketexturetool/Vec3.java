package de.maikmerten.quaketexturetool;

/**
 *
 * @author maik
 */
public class Vec3 {
	
	private double x,y,z;
	
	public Vec3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	public double getLength() {
		return Math.sqrt(x*x + y*y + z*z);
	}
	
	public void scale(double s) {
		x = x * s;
		y = y * s;
		z = z * s;
	}
	
	public void normalize() {
		scale(1.0 / getLength());
	}
	
	
	public Vec3 dot(Vec3 v) {
		double a1 = x;
		double a2 = y;
		double a3 = z;
		
		double b1 = v.x;
		double b2 = v.y;
		double b3 = v.z;
		
		double c1 = a2*b3 - a3*b2;
		double c2 = a3*b1 - a1*b3;
		double c3 = a1*b2 - a2*b1;
		
		return new Vec3(c1, c2, c3);
	}
	
	
}
