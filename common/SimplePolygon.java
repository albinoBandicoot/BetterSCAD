package common ;
public class  SimplePolygon extends Node2D {

	// single closed loop, no holes. May be concave. Vertices ordered by right hand rule.
	public Float3[] points;
	private Float3[] vecs;
	private Float3[] normals;
	private double[] veclens;

	public SimplePolygon (Float3[] pts) {
		this.points = pts;
		vecs = new Float3[pts.length];
		veclens = new double[pts.length];
		normals = new Float3[pts.length];
		Float3 up = new Float3 (0,0,1);
		for (int i=0; i<points.length; i++) {
			vecs[i] = points[(i+1)%points.length].sub(points[i]);
			normals[i] = up.cross(vecs[i]).normalize();
			veclens[i] = vecs[i].mag();
		}
	}

	public Node copy () {
		return new SimplePolygon (points);
	}

	public double csg (Float3 pt) {
		// minimum of distances to each segment.
		double maxd = -1e20;
		Float3 p = new Float3 (pt.x, pt.y, 0);
		for (int i=0; i<points.length; i++) {
			Float3 q = p.sub(points[i]);
			double d = q.dot (normals[i]);
			maxd = Math.max (d, maxd);
		}
		return maxd;
	}
			
	public int findIptsMax () {
		return points.length;
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
		Float3 a = new Float3 (r.start.x, r.start.y, 0);
		Float3 d = new Float3 (r.dir.x, r.dir.y, 0);
		double dmag = d.mag();
		d = d.normalize();
		int ict = 0;
		for (int i=0; i < points.length; i++) {
			// get intersection of ray with segment
			Float3 q = a.sub(points[i]);
			double sy = q.dot(normals[i]);	// length of projection of position vec onto normal
			double dy = d.dot(normals[i]);	// length of projection of direction vec onto normal
			if (Math.abs(dy) > 1e-8) {	// nonparallel
				double t = -sy/dy;
				if (t >= 0) {
					Float3 proj = a.add(d.mul(t)).sub(points[i]);
					double s = 0;
					if (Math.abs(proj.x) > Math.abs(proj.y)) {
						s = proj.x / vecs[i].x;
					} else {
						s = proj.y / vecs[i].y;
					}
					if (s >= 0 && s <= 1) {
						ict++;
						il.add (new Intersection (t/dmag, this, i));
					}
				}
			}
		}
		return ict;
	}

	public String getString () {
		return "Polygon";
	}

}
