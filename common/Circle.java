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
	
	public double dist (Float3 pt) {
		return Math.sqrt (pt.x*pt.x + pt.y*pt.y) - rad;
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
		il.add (new Intersection ((s-q) / dmag, this, 0));
		il.add (new Intersection ((s+q) / dmag, this, 0));
		return 2;
	}

	public String getString () {
		return "Circle, r = " + rad;
	}

}
