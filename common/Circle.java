package common ;
import java.util.ArrayList;
public class  Circle extends Node {

	/* Unit circle on the xy-plane */

	public Circle () {
	}

	public double csg (Float3 pt) {
		return Math.max (pt.z, Math.sqrt(pt.x*pt.x + pt.y*pt.y) - 1);
	}

	public Intersection intersection (Ray r) {
		/* Basically the same as the sphere code, but setting the z components to 0. Unfortunately,
		 * this may cause a 0-length direction vector, which has to be special-cased. */
		Float3 omc = r.start;
		Float3 dir = r.dir;
		omc.z = 0;
		dir.z = 0;
		if (dir.mag() < 1e-6) {
			if (Math.abs(csg (omc)) < 1e-6) {	// the ray pierces the edge of the circle from directly above
				return new Intersection (0, this);
			}
			return Intersection.NONE;
		}
		double b = dir.dot (omc);
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
		Float3 dir = r.dir;
		omc.z = 0;
		dir.z = 0;
		if (dir.mag() < 1e-6) {
			if (Math.abs(csg (omc)) < 1e-6) {	// the ray pierces the edge of the circle from directly above
				ipts.add (new Intersection(0,this));
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
			ipts.add (new Intersection (t1, this));
		}
		if (t0 > 0) {
			ipts.add (new Intersection (t0, this));
		}
		return ipts;
	}

}
