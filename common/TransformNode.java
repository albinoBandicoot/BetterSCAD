package common;
import java.util.ArrayList;
public class TransformNode extends Node {

	public Transform xform;

	public TransformNode (Transform t) {
		xform = t;
	}

	public double csg (Float3 pt) {
		return left.csg (xform.transformPoint (pt));
	}

	public Intersection intersection (Ray r) {
		return left.intersection (xform.transformRay (r));
	}

	public ArrayList<Intersection> allIntersections (Ray r) {
		return left.allIntersections (xform.transformRay (r));
	}

	public String getString () {
		return "Xform mat = " + xform;
	}
}
