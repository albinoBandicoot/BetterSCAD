package common ;
import java.util.ArrayList;
public class  Sphere extends Node {

	/* This just defines the unit sphere centered at the origin. All other spheres are made by transformations */

	public Sphere () {
	}

	public double csg (Float3 p) {
		return p.mag() - 1;
	}

	/* This only returns the first (closest) positive intersection with the sphere, or null if there is no 
	 * intersection with a positive t value */
	public Intersection intersection (Ray r) {
		/* See p 739 of Real Time Rendering */
		Float3 omc = r.start;
		double b = r.dir.dot (omc);
		double c = omc.dot(omc) - 1;
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

	public ArrayList<Intersection> allIntersections (Ray r) {
		ArrayList<Intersection> ipts = new ArrayList<Intersection>();
		Float3 omc = r.start;
		double b = r.dir.dot (omc);
		double c = omc.dot(omc) - 1;
		if (b*b - c < 0) {
			return ipts;
		}
		double d = Math.sqrt (b*b-c);
		double t0 = -b + d;
		double t1 = -b - d;
		if (t1 > 0) {
			ipts.add (new Intersection(t1, this));
		}
		if (t0 > 0) {
			ipts.add (new Intersection (t0, this));
		}
		return ipts;
	}
}
