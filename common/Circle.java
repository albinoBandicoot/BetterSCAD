package common ;
import java.util.ArrayList;
public class  Circle extends Node2D {

	/* Origin-centered circle on the xy-plane */

	public double rad = 1;

	public Circle () {
	}

	public Circle (double r) {
		this.rad = r;
	}

	public Node copy () {
		return new Circle (rad);
	}

	public double csg (Float3 pt) {
		return Math.sqrt(pt.x*pt.x + pt.y*pt.y) - rad;
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

	public int allContourIntersections (IList il, Ray r) {
		/* Ripped pretty much directly out of 'Real-Time Rendering' page 741. */
		Float3 l = new Float3 (-r.start.x, -r.start.y, 0);
		Float3 d = new Float3 (r.dir.x, r.dir.y, 0);
		if (d.mag() < 1e-6) return 0;
		double dmag = d.mag();
		d = d.normalize();
		double s = l.dot(d);
		double lsq = l.dot(l);
		if (s < 0 && lsq > rad*rad) return 0;
		double msq = lsq - s*s;
		if (msq > rad*rad) return 0;
		double q = Math.sqrt (rad*rad - msq);
		il.add (new Intersection ((s-q) / dmag, this, true));
		il.add (new Intersection ((s+q) / dmag, this, true));
		return 2;
	}

	/*
	public ArrayList<Intersection> allContourIntersections (Ray r) {
		ArrayList<Intersection> ipts = new ArrayList<Intersection>();
		Float3 omc = new Float3 (r.start.x, r.start.y, 0).normalize();
		Float3 dir = new Float3 (r.dir.x, r.dir.y, 0).normalize();
		if (dir.mag() < 1e-6) { 
			return ipts;	// empty
		}
		double b = dir.dot (omc);
		double c = omc.dot(omc) - rad*rad;
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
	*/

	public String getString () {
		return "Circle, r = " + rad;
	}

}
