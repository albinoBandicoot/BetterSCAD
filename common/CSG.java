package common ;
import java.util.ArrayList;
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

	public double csg (Float3 pt) {
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

	public ArrayList<Intersection> allIntersections (Ray r) {
		ArrayList<Intersection> left_inter = left.allIntersections(r);
		ArrayList<Intersection> right_inter = right.allIntersections(r);
		ArrayList<Intersection> res = new ArrayList<Intersection>();
		for (Intersection i : left_inter) {
			if (onSurface (r.get(i.t))) {
				res.add (i);
			}
		}
		for (Intersection i : right_inter) {
			if (onSurface (r.get(i.t))) {
				res.add (i);
			}
		}
		return res;
	}
}
