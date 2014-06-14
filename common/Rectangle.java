package common ;
import java.util.ArrayList;
public class  Rectangle extends Node2D {

	/* Rectangle with bottom left corner on origin, in the xy plane */

	public double xs, ys;

	public Rectangle () {
		xs = ys = 1;
	}

	public Rectangle (double xs, double ys) {
		this.xs = xs;
		this.ys = ys;
	}

	public Node copy () {
		return new Rectangle (xs, ys);
	}

	public double csg (Float3 pt) {
		double x = Math.abs(pt.x - xs/2) - xs/2;
		double y = Math.abs(pt.y - ys/2) - ys/2;
		return Math.max(x,y);
	}

	public double dist (Float3 pt) {
		if (pt.x >= 0 && pt.x <= xs) {
			return Math.abs (pt.y - ys/2) - ys/2;
		} else if (pt.y >= 0 && pt.y <= ys) {
			return Math.abs (pt.x - xs/2) - xs/2;
		}
		// we're in one of the four corner regions. We'll shift these to be centered all at the origin
		// and compute the distance from the origin.
		double x = pt.x >= xs ? pt.x-xs : pt.x;
		double y = pt.y >= ys ? pt.y-ys : pt.y;
		return Math.sqrt (x*x + y*y);
	}

	public int findIptsMax () {
		return 2;
	}

	public void allIntersections (IList il, Ray r) {
		Intersection i = Intersection.build (r, 0, this);
		if (i != null) {
			Float3 ipt = r.get (i.t);
			if (csg (ipt) < 0) {
				il.add (i);
			}
		}
	}

	private Intersection testX (double x, Ray r) {
		double t = r.solveX (x);
		double y = r.get(t).y;
		if (y >= 0 && y <= ys) {
			return new Intersection (t, this, x==0? 0 : 2);
		}
		return null;
	}

	private Intersection testY (double y, Ray r) {
		double t = r.solveY (y);
		double x = r.get(t).x;
		if (x >= 0 && x <= xs) {
			return new Intersection (t, this, y==0 ? 1 : 3);
		}
		return null;
	}

	public int allContourIntersections (IList il, Ray r) {
		int nsave = il.n;
		if (Math.abs(r.dir.x) > 1e-8) {
			Intersection x0 = testX (0, r);
			Intersection xxs = testX (xs, r);
			if (x0 != null) il.add (x0);
			if (xxs != null) il.add (xxs);
		}
		if (Math.abs(r.dir.y) > 1e-8) {
			Intersection y0 = testY (0, r);
			Intersection yys = testY (ys, r);
			if (y0 != null) il.add (y0);
			if (yys != null) il.add (yys);
		}
		return il.n - nsave;
	}

	public String getString () {
		return "Rectangle, (xs,ys) = " + xs + ", " + ys;
	}

}
