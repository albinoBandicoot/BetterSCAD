package common ;
import java.util.ArrayList;
public class  CSG extends Node {

	public static final int UNION = 0;
	public static final int INTERSECTION = 1;
	public static final int DIFFERENCE = 2;

	public int type;

	public Node[] children;

	public CSG () {
		type = UNION;
	}

	public CSG (int type) {
		this.type = type;
	}

	public Node copy () {
		return new CSG (type);
	}

	public CSG (String typename) {
		String n = typename.toLowerCase();
		if (n.equals ("union")) {
			type = UNION;
		} else if (n.equals ("intersection")) {
			type = INTERSECTION;
		} else if (n.equals ("difference")) {
			type = DIFFERENCE;
		} else {
			// error
			type = 0;
		}
	}

	public double csg (Float3 pt) {
		double res = (type == UNION) ? 1e20 : -1e20;
		for (int i=0; i<children.length; i++) {
			double c = children[i].csg (pt);
			switch (type) {
				case UNION:
					res = Math.min (res, c);	break;
				case INTERSECTION:
					res = Math.max (res, c);	break;
				case DIFFERENCE:
					res = Math.max (res, i==0 ? c : -c);
			}
		}
		return res;
		/*
		switch (type) {
			case UNION:
				return Math.min (left.csg(pt), right.csg(pt));
			case INTERSECTION:
				return Math.max (left.csg(pt), right.csg(pt));
			case DIFFERENCE:
				return Math.max (left.csg(pt), -right.csg(pt));
		}
		*/
	}

	public double dist (Float3 pt) {
		return csg (pt);
	}

	public int findIptsMax () {
		int ct = 0;
		for (int i=0; i<children.length; i++) {
			ct += children[i].findIptsMax();
		}
		return ct;

	//	return left.findIptsMax() + right.findIptsMax();
	}

	public void allIntersections (IList il, Ray r) {
		for (int i=0; i<children.length; i++) {
			children[i].allIntersections (il, r);
		}
		/*
		left.allIntersections(il, r);
		right.allIntersections(il, r);
		*/
		/*
		int nsave = il.n;
		for (int i=nsave; i<il.n; i++) {
			if (il.ints[i] != null && !onSurface (r.get(il.ints[i].t))) {
				il.ints[i] = null;
			}
		}
		*/
	}

	public String getString () {
		switch (type) {
			case UNION:	return "Union";
			case INTERSECTION: return "Intersection";
			case DIFFERENCE: return "Difference";
			default: return "BAD CSG TYPE";
		}
	}
}
