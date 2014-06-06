package common;
import java.util.ArrayList;

public abstract class Node {

	public Node left, right, parent;
	public Material mat;

	public abstract double csg (Float3 pt);
	public double dist (Float3 pt) {
		return csg (pt);
	}

	public abstract Node copy ();

	public abstract int findIptsMax ();

	public final boolean onSurface (Float3 pt) {
		return Math.abs(csg(pt)) < 1e-6;
	}

	public final Intersection intersection (IList il, Ray r) {
		il.clear();
		allIntersections (il, r);

		/*
		Intersection[] ints = il.getSorted();
		il.n = 0;
		if (ints.length == 0) return Intersection.NONE;
		int idx = 0;
		while (idx < ints.length && !onSurface (r.get(ints[idx].t))) {
			il.csg_ct ++;
			idx ++;
		}
		if (idx == ints.length) return Intersection.NONE;
		return ints[idx];
		*/

		Intersection res = null;
		double mint = 123456789;
		
		for (int x=0; x<il.n; x++) {
			Intersection i = il.ints[x];
			if (i != null && i.t < mint) {
				if (onSurface (r.get(i.t))) {
					il.csg_ct ++;
					mint = i.t;
					res = i;
				}
			}
		}
		il.n = 0;	// very important to reset!
		if (res == null) {
			return Intersection.NONE;
		}
		return res;
	}

	public abstract void allIntersections (IList il, Ray r);

	// something for the plane intersection; perhaps there should be an interface that gives a closed-form curve for the plane intersection that a subset of the nodes implement.
	
	public abstract String getString ();

	public final String toString () {
		return stringify (0);
	}

	private String stringify (int depth) {
		StringBuilder sb = new StringBuilder ();
		for (int i=0; i<depth; i++) {
			sb.append ("    ");
		}
		sb.append (getString());
		if (left != null) {
			sb.append("\n");
			sb.append(left.stringify (depth+1) + "\n");
		} else {
			if (right != null) {
				sb.append ("<null>\n");
			}
		}
		if (right != null) {
			sb.append (right.stringify (depth+1) + "\n");
		}
		if (this instanceof CSG) {
			sb.append ("\n");
			CSG c = (CSG) this;
			for (Node n : c.children) {
				sb.append (n.stringify (depth+1) + "\n");
			}
		}
		return sb.toString();
	}
	
}

