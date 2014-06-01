package common ;
public class  Ray {

	public Float3 start, dir;

	public Ray (Float3 s, Float3 d) {
		start = s;
		dir = d;
	}

	public Float3 get (double t) {
		return start.add (dir.mul(t));
	}

	public double solveX (double x) {
		return (x - start.x) / dir.x;
	}

	public double solveY (double y) {
		return (y - start.y) / dir.y;
	}

	public double solveZ (double z) {	// get the t value that produces this z value.
		return (z - start.z) / dir.z;
	}

	public String toString () {
		return start + " --> " + dir;
	}
}
