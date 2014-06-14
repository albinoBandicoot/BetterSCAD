package common ;
import java.util.ArrayList;
public class  Extrude extends Node {

	public double h;

	public Extrude (double h) {
		this.h = h;
	}

	public Node copy () {
		return new Extrude (h);
	}
	
	/* Has one child, the 2D object to extrude. Extrusion height is h, along +z. All 2D objects inhabit the
	 * xy-plane, so no z translations have any effect on them. They can be rotated out of the plane (openSCAD
	 * permits this), and 3D generative operations work on their projection into the xy-plane. I am actually 
	 * very tempted not to allow this behavior. */

	public double csg (Float3 pt) {
		double r = left.csg (pt);
		double z = 0;
		if (pt.z < h/2) {
			z = -pt.z;
		} else {
			z = pt.z - h;
		}
		return Math.max (z, r);
	}

	public double dist (Float3 pt) {
		// for now, just for straight extrudes.
		double d_inf = left.dist (pt);
		if (pt.z > h) {
			if (d_inf <= 0) return pt.z - h;
			double zleg = pt.z - h;
			return Math.sqrt (zleg*zleg + d_inf*d_inf);
		} else if (pt.z < 0) {
			if (d_inf <= 0) return -pt.z;
			double zleg = -pt.z;
			return Math.sqrt (zleg*zleg + d_inf*d_inf);
		} else {
			return d_inf;
		}
	}

	public int findIptsMax () {
		return 2 + left.findIptsMax();
	}

	public void allIntersections (IList il, Ray r) {
		int ninter = ((Node2D) left).allContourIntersections (il, r);
		int deleted = 0;
		for (int i=0; i<ninter; i++) {
			double csgval = csg (r.get (il.ints[il.n - i - 1].t));
			if (csgval - 1e-6 > 0) {
				il.ints[il.n - i - 1] = null;
				deleted ++;
			}
		}
		Intersection topi = Intersection.build (r, h, this);
		Intersection boti = Intersection.build (r, 0, this);
		if (topi != null && onSurface (r.get(topi.t))) {
			topi.facet = -1;
			il.add (topi);
		}
		if (boti != null && onSurface (r.get(boti.t))) {
			boti.facet = -2;
			il.add (boti);
		}
	}

	public String getString () {
		return "Extrude h = " + h;
	}

}
