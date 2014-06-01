package common;
import java.util.ArrayList;
public class TransformNode extends Node {

	public Transform xform;
	public Transform inverse;

	public TransformNode (Transform t) {
		xform = t;
		inverse = t.invert();
		Transform TEST = xform.append (inverse);
		System.out.println ("INVERSION TEST: (original = " + xform + "; inverse = " + inverse + ") product = " + TEST);
	}

	public Node copy () {
		return new TransformNode (xform);
	}

	public double csg (Float3 pt) {
		return left.csg (inverse.transformPoint (pt));
	}

	public Intersection intersection (Ray r) {
		return left.intersection (inverse.transformRay (r));
	}

	public ArrayList<Intersection> allIntersections (Ray r) {
		return left.allIntersections (inverse.transformRay (r));
	}

	public String getString () {
		return "Xform mat = " + xform + "; inverse = " + inverse;
	}
}
