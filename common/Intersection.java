package common;
public class Intersection {

	public double t;
	public Node obj;
	public boolean flag;

	public static final Intersection NONE = new Intersection (-1, null);

	public Intersection (double t, Node n) {
		this.t = t;
		this.obj = n;
		this.flag = false;
	}

	public Intersection (double t, Node n, boolean flag) {
		this.t = t;
		this.obj = n;
		this.flag = flag;
	}

	public static Intersection build (Ray r, double z, Node n) {
		if (Math.abs(r.dir.z) > 1e-8) {
			return new Intersection ((z - r.start.z) / r.dir.z, n);
		} else {
			return NONE;
		}
	}

}
