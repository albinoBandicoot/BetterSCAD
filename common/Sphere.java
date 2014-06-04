package common ;
import java.util.ArrayList;
public class  Sphere extends Node {

	/* This just defines spheres centered at the origin. All other spheres are made by transformations */

	public double rad;

	public Sphere () {
		rad = 1;
	}

	public Sphere (double r) {
		this.rad = r;
	}

	public Node copy () {
		return new Sphere (rad);
	}

	public double csg (Float3 p) {
		return p.mag() - rad;
	}

	public int findIptsMax () {
		return 2;
	}

	/* This only returns the first (closest) positive intersection with the sphere, or null if there is no 
	 * intersection with a positive t value */
	public Intersection intersection (Ray r) {
		/* See p 739 of Real Time Rendering */
		Float3 omc = r.start;
		double b = r.dir.dot (omc);
		double c = omc.dot(omc) - rad*rad;
		if (b*b - c < 0) {
			return Intersection.NONE;
		}
		double d = Math.sqrt (b*b-c);
		double t0 = -b + d;
		double t1 = -b - d;
		if (t1 > 0) {
			return new Intersection (t1, this);
		} else if (t0 > 0) {
			return new Intersection (t0, this);
		}
		return null;
	}

	public void allIntersections (IList il, Ray r) {
		Float3 omc = r.start;
		double b = r.dir.dot (omc);
		double c = omc.dot(omc) - rad*rad;
		if (b*b - c < 0) {
			return;
		}
		double d = Math.sqrt (b*b-c);
		double t0 = -b + d;
		double t1 = -b - d;
		if (t1 > 0) {
			il.add (new Intersection(t1, this));
		}
		if (t0 > 0) {
			il.add (new Intersection (t0, this));
		}
	}

	public String getString () {
		return "Sphere, r = " + rad;
	}
}
