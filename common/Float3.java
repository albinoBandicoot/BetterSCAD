package common;
public class Float3 {

	public double x,y,z;

	public Float3 () {
		x=0;
		y=0;
		z=0;
	}

	public Float3 (double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Float3 add (Float3 other){
		return new Float3 (x + other.x, y + other.y, z + other.z);
	}

	public Float3 sub (Float3 other){
		return new Float3 (x - other.x, y - other.y, z - other.z);
	}

	public Float3 mul (double d){
		return new Float3 (x*d, y*d, z*d);
	}

	public double mag () {
		return Math.sqrt (x*x + y*y + z*z);
	}

	public double magsq () {
		return x*x + y*y + z*z;
	}

	public double dist (Float3 other){
		return Math.sqrt ((other.x-x)*(other.x-x) + (other.y-y)*(other.y-y) + (other.z-z)*(other.z-z));
	}

	public double dot (Float3 other){
		return x * other.x + y * other.y + z*other.z;
	}

	public Float3 cross (Float3 a){
		return new Float3 (y*a.z - a.y*z, z*a.x - a.z*x, x*a.y - a.x*y);
	}

	public Float3 normalize () {
		return mul (1/mag());
	}

	public Float3 clamp () {
		return new Float3 (clamp(x), clamp(y), clamp(z));
	}

	public static double clamp (double x) {
		return x > 1 ? 1 : (x < 0 ? 0 : x);
	}

	public void jitter (double fac){
		x += fac * (Math.random() - 0.5);
		y += fac * (Math.random() - 0.5);
		z += fac * (Math.random() - 0.5);
	}

	public static Float3 randomIn (Float3 corner, Float3 size){
		double x = Math.random () * size.x + corner.x;
		double y = Math.random () * size.y + corner.y;
		double z = Math.random () * size.z + corner.z;
		return new Float3(x,y,z);
	}

	public Float3 rotateX (double theta){
		double cos = Math.cos(theta);
		double sin = Math.sin(theta);
		return new Float3 (x, cos*y - sin*z, sin*y + cos*z);
	}

	public Float3 rotateZ (double theta){
		double cos = Math.cos(theta);
		double sin = Math.sin(theta);
		return new Float3 (cos*x - sin*y, sin*x + cos*y, z);
	}

	public Float3 axisRotate (Float3 axis, double theta){	// assumes axis is normalized
		/* Formula from http://inside.mines.edu/fs_home/gmurray/ArbitraryAxisRotation/ */
		double u = axis.x;
		double v = axis.y;
		double w = axis.z;
		double sin = Math.sin(theta);
		double cos = Math.cos(theta);
		double Q = (u*x + v*y + w*z) * (1-cos);
		double rx = u*Q + x*cos + (v*z - w*y)*sin;
		double ry = v*Q + y*cos + (w*x - u*z)*sin;
		double rz = w*Q + z*cos + (u*y - v*x)*sin;
		return new Float3 (rx, ry, rz);
	}	

	public int getRGB () {
		return 256*256*((int) (x*255)) + 256*((int) (y*255)) + (int) (z*255);
	}

	public String toString () {
		return "(" + x + ", " + y + ", " + z + ")";
	}

}
