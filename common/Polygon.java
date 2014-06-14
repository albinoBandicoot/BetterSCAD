package common ;
public class  Polygon extends Node2D {

	public Float3[] points;
	public int[][] ind;

	public Polygon (Float3[] pts, int[][] ind) {
		this.points = pts;
		this.ind = ind;
	}

	public Node copy () {
		return new Polygon (points, ind);
	}


	public double csg (Float3 pt) {
		//
		return 1;
	}


	public int findIptsMax () {
		return 2;	// + 2 * #notches + #holes
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
		Float3 b = new Float3 (r.start.x, r.start.y, 0);
		Float3 e = new Float3 (r.dir.x, r.dir.y, 0);
		double emag = e.mag();
		e = e.normalize();
		int ict = 0;
		for (int path=0; path < ind.length; path++) {

			for (int i=0; i < ind[path].length; i++) {
				// get intersection of ray with segment
				Float3 a = points[ind[path][i]];
				Float3 v2 = points[ind[path][(i+1)%points.length]];
				Float3 d = v2.sub(a);
				double t = ((b.x-a.x)/e.y)*(1+d.y/d.x)/(1 - e.x*d.y/(d.x*e.y));
				Float3 ipt = b.add (e.mul(t));
				double s = -1;
				if (Math.abs(d.x) > Math.abs(d.y)) {
					s = (ipt.x - a.x) / d.x;
				} else {
					s = (ipt.y - a.y) / d.y;
				}
				if (s >= 0 && s <= 1) {
					ict++;
					il.add (new Intersection (t, this, -1));
				}
			}
		}
		return ict;
	}

	public String getString () {
		return "Polygon";
	}

}
