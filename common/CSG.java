package common ;
public class  CSG extends Node {

	public static final int UNION = 0;
	public static final int INTERSECTION = 1;
	public static final int DIFFERENCE = 2;

	public int type;

	public CSG () {
		type = UNION;
	}

	public CSG (int type) {
		this.type = type;
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

	public double csg (Float3 p) {
		Float3 pt = xform == null ? p : xform.transformPoint (p);
		switch (type) {
			case UNION:
				return Math.min (left.csg(pt), right.csg(pt));
			case INTERSECTION:
				return Math.max (left.csg(pt), right.csg(pt));
			case DIFFERENCE:
				return Math.max (left.csg(pt), -right.csg(pt));
		}
		return 1;
	}

	public ArrayList<Double> allIntersections (Ray r) {
		ArrayList<Double> left_inter = left.allIntersections(r);
		ArrayList<Double> right_inter = right.allIntersections(r);
		ArrayList<Double> res = new ArrayList<Double>();
		for (double t : left_inter) {
			if (onSurface (r.get(t))) {
				res.add (t);
			}
		}
		for (double t : right_inter) {
			if (onSurface (r.get(t))) {
				res.add (t);
			}
		}
	}
}
