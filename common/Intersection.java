package common;
public class Intersection implements Comparable {

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
			return null;
		}
	}

	public int compareTo (Object other) {
		if (other instanceof Intersection) {
			Intersection i = (Intersection) other;
			if (t == i.t) return 0;
			if (t < i.t) return -1;
			return 1;
		}
		return -1;
	}

	public String toString () {
		return "Int @ t = " + t;
	}
}
