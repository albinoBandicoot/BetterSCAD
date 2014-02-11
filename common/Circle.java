package common ;
public class  Circle {

	/* Unit circle on the xy-plane */

	public Circle () {
	}

	public double csg (Float3 p) {
		Float3 pt = xform == null ? p : xform.transformPoint (p);
		return Math.max (pt.z, Math.sqrt(pt.x*pt.x + pt.y*pt.y) - 1);
	}

	public double intersection (Ray r) {
		/* Basically the same as the sphere code, but setting the z components to 0. Unfortunately,
		 * this may cause a 0-length direction vector, which has to be special-cased. */
		Float3 omc = r.start;
		Float3 dir = r.dir;
		omc.z = 0;
		dir.z = 0;
		if (dir.mag() < 1e-6) {
			if (Math.abs(csg (omc)) < 1e-6) {	// the ray pierces the edge of the circle from directly above
				return 0;
			}
			return -1;
		}
		double b = dir.dot (omc);
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
		Float3 dir = r.dir;
		omc.z = 0;
		dir.z = 0;
		if (dir.mag() < 1e-6) {
			if (Math.abs(csg (omc)) < 1e-6) {	// the ray pierces the edge of the circle from directly above
				ipts.add (0);
			}
			return ipts;
		}
		double b = dir.dot (omc);
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
			ipts.add (t0);
		}
		return ipts;
	}

}
}
