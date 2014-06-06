package common ;
import java.util.ArrayList;
public class  Revolve extends Node {

	public Revolve () {
	}

	public Node copy () {
		return new Revolve();
	}

	public double csg (Float3 pt) {
		return 1;
	}

	public double dist (Float3 pt) {
		return 1;
	}

	public int findIptsMax () {
		return 2*left.findIptsMax();	// this is just a guess that feels right.
	}

	public void allIntersections (IList il, Ray r) {
	}

	public String getString () {
		return "Revolve";
	}

}
