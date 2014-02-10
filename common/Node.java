package common;

public abstract class Node {

	public Node left, right;

	public abstract double csg (Float3 pt);

	public final boolean onSurface (Float3 pt) {
		return Math.abs(csg(pt)) < 1e-8;
	}

	public abstract Float3 intersection (Ray r);

	// something for the plane intersection; perhaps there should be an interface that gives a closed-form curve for the plane intersection that a subset of the nodes implement.
	
}

