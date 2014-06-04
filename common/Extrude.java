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

	public int findIptsMax () {
		return 2 + left.findIptsMax();
	}

	public void allIntersections (IList il, Ray r) {
		int ninter = ((Node2D) left).allContourIntersections (il, r);
		int deleted = 0;
		for (int i=0; i<ninter; i++) {
			double csgval = csg (r.get (il.ints[il.n - i - 1].t));
			if (csgval - 1e-8 > 0) {
				il.ints[il.n - i - 1] = null;
				deleted ++;
			}
		}
		Intersection topi = Intersection.build (r, h, this);
		Intersection boti = Intersection.build (r, 0, this);
		if (topi != null && onSurface (r.get(topi.t))) {
			il.add (topi);
		}
		if (boti != null && onSurface (r.get(boti.t))) {
			il.add (boti);
		}
	}


	/* First find the intersection points with the infinite cylinder. Find the min and max z values, then
	 * remove anything outside [0..h]. Depending on the min and max vals, intersections with the caps may
	 * be added. */
	/*
	public ArrayList<Intersection> allIntersections (Ray r) {
		ArrayList<Intersection> icyl = ((Node2D) left).allContourIntersections (r);
		double minz = 1e50;
		double maxz = -1e50;
		double minzt, maxzt;
		boolean vertical = new Float3 (r.dir.x, r.dir.y, 0).mag() < 1e-6;
		if (!vertical) {
			for (int i=0; i<icyl.size(); i++) {
				double z = r.get (icyl.get(i).t).z;
				if (z < minz) {
					minz = z;
					minzt = icyl.get(i).t;
				}
				if (z > maxz) {
					maxz = z;
					maxzt = icyl.get(i).t;
				}
				System.out.print ("z = " + z);
				if (z < 0 || z > h) {
					System.out.println("; removing");
					icyl.remove (i);
					i--;
				} else {
					System.out.println("; not removing");
				}

			}
		}
		System.out.print ("ICYL before caps = " + icyl.size() + "   ");
		// if minz > 0, we can eliminate the bottom cap test, and if maxz < h, we can eliminate the top cap test.
		if (vertical || minz <= 0) {	// we need the bottom cap intersection
			Intersection bot = Intersection.build (r, 0, this);
			if (bot != Intersection.NONE && left.csg (r.get(bot.t)) < 0) {
				icyl.add (bot);
			}
		}
		if (vertical || maxz >= h) {	// we need the top cap intersection
			Intersection top = Intersection.build (r, h, this);
			if (top != Intersection.NONE && left.csg (r.get(top.t)) < 0) {
				icyl.add (top);
			}
		}
		System.out.println ("ICYL len = " + icyl.size());
		return icyl;
	}
	*/

	public String getString () {
		return "Extrude h = " + h;
	}

}
