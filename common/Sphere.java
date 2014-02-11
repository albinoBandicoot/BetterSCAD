package common ;
public class  Sphere extends Node {

	/* This just defines the unit sphere centered at the origin. All other spheres are made by transformations */

	public Sphere () {
	}

	public double csg (Float3 p) {
		Float3 pt = xform == null ? p : xform.transformPoint (p);
		return pt.mag() - 1;
	}

	/* This only returns the first (closest) positive intersection with the sphere, or null if there is no 
	 * intersection with a positive t value */
	public double intersection (Ray r) {
		/* See p 739 of Real Time Rendering */
		Float3 omc = r.start;
		double b = r.dir.dot (omc);
		double c = omc.dot(omc) - 1;
		if (b*b - c < 0) {
			return -1;
		}
		double d = Math.sqrt (b*b-c);
		double t0 = -b + d;
		double t1 = -b - d;
		if (t1 > 0) {
			return t1;
		} else if (t0 > 0) {
			return t0;
		}
		return null;
	}

	public ArrayList<Double> allIntersections (Ray r) {
		ArrayList<Double> ipts = new ArrayList<Double>();
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
			ipts.add (t1);
		}
		if (t0 > 0) {
			ipts.add (t1);
		}
		return ipts;
	}
}
