package common ;
import java.util.ArrayList;
public class  Extrude extends Node {

	public double h;

	public Extrude (double h) {
		this.h = h;
	}
	
	/* Has one child, the 2D object to extrude. Extrusion height is h, along +z. All 2D objects inhabit the
	 * xy-plane, so no z translations have any effect on them. They can be rotated out of the plane (openSCAD
	 * permits this), and 3D generative operations work on their projection into the xy-plane. I am actually 
	 * very tempted not to allow this behavior. */

	public double csg (Float3 pt) {
		double r = left.csg (pt);
		double z = 0;
		if (pt.z < 0) {
			z = -pt.z;
		} else if (pt.z > h) {
			z = pt.z - h;
		}
		return Math.max (z, r);
	}

	public Intersection intersection (Ray r) {
		/* First find the intersection points (t-coords) with the infinite cylinder. (this can
		 * be done by doing the intersection test in the xy plane on the 2D child object). Then determine
		 * whether these are in the allowed Z coordinate range for the cylinder [0..h]. 
		 *
		 * Now also determine the intersection of the ray and the top and bottom plane and if they are inside
		 * the shape or its shifted copy. 
		 *
		 * Take all of these intersections and return the closest one.
		 */
		return Intersection.NONE;
	}

	public ArrayList<Intersection> allIntersections (Ray r) {
		return null;
	}

	public String getString () {
		return "Extrude h = " + h;
	}

}
