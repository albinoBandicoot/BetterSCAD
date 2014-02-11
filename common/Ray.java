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
}
