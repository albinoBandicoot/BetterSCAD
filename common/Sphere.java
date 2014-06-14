package common ;
import java.util.ArrayList;
public class  Sphere extends Node {

	/* This just defines spheres centered at the origin. All other spheres are made by transformations */

	public double rad;

	public Sphere () {
		rad = 1;
	}

	public Sphere (double r) {
		this.rad = r;
	}

	public Node copy () {
		return new Sphere (rad);
	}

	public double csg (Float3 p) {
		return p.mag() - rad;
	}

	public double dist (Float3 p) {
		return p.mag() - rad;
	}

	public int findIptsMax () {
		return 2;
	}

	public void allIntersections (IList il, Ray r) {
		Float3 omc = r.start;
		Float3 dir = r.dir.normalize();
		double b = dir.dot (omc);
		double c = omc.dot(omc) - rad*rad;
		if (b*b - c < 0) {
			return;
		}
		double d = Math.sqrt (b*b-c);
		double t0 = -b + d;
		double t1 = -b - d;
		if (t1 > 0) {
			il.add (new Intersection(t1/r.dir.mag(), this, 0));
		}
		if (t0 > 0) {
			il.add (new Intersection (t0/r.dir.mag(), this, 0));
		}
	}

	public String getString () {
		return "Sphere, r = " + rad;
	}
}
