package common;
public class Intersection {

	public double t;
	public Node obj;

	public static final Intersection NONE = new Intersection (-1, null);

	public Intersection (double t, Node n) {
		this.t = t;
		this.obj = n;
	}

}
