package render;
import common.*;
public class Light {

	public Float3 dir, col;

	public Light (Float3 dir, Float3 col) {
		this.dir = dir.normalize();
		this.col = col;
	}

	public Float3 getContrib (Float3 pt, Float3 view, Float3 grad) {
		return col.mul (clamp (dir.dot(grad)) * Math.abs (view.dot(grad)));
	}

	public static double clamp (double x) {
		return x < 0 ? 0 : (x > 1 ? 1 : x);
	}

}
