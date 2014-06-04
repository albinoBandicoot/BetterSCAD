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

	public int findIptsMax () {
		return left.findIptsMax();
	}

	public void allIntersections (IList il, Ray r) {
		left.allIntersections (il, inverse.transformRay (r));
	}

	public String getString () {
		return "Xform mat = " + xform + "; inverse = " + inverse;
	}
}
