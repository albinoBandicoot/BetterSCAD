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

	public ArrayList<Intersection> allIntersections (Ray r) {
		return null;
	}

	public String getString () {
		return "Revolve";
	}

}
