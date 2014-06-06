package render ;
import common.*;
public class  Camera {

	public Float3 bl, dir, up, right;
	public int scrx, scry;

	public Camera () {
		this (new Float3 (-10, -10, 10), new Float3 (0, 0, -1), new Float3 (0, 20, 0), 200, 200);
	}

	public Camera (int xs, int ys) {
		this (new Float3 (-((double) xs)/ys * 10, -10, 10), new Float3 (0, 0, -1), new Float3 (0, 20, 0), xs, ys);
	}

	public Camera (Float3 bl, Float3 dir, Float3 up, int scrx, int scry) {
		this.bl = bl;
		this.dir = dir.normalize();
		this.up = up;
		this.scrx = scrx;
		this.scry = scry;
		double ar = scrx / ((double) scry);
		this.right = dir.cross(up).normalize().mul (up.mag() * ar);
	}

	public Float3 project (Float3 p) {	// project to [0..1]
		Float3 pt = p.sub(bl);
		return new Float3 (pt.dot(right) / right.magsq(), pt.dot(up) / up.magsq(), pt.dot(dir) / dir.magsq());
	}

	/* pt is the point about which to rotate, d is the new direction for the camera,
	 * and x and y are the projected coordinates of pt in the old camera */
	public Camera lookAt (Float3 pt, Float3 d) {
		Float3 oldproj = project (pt);
		double x = oldproj.x;
		double y = oldproj.y;
		
		Camera cam = new Camera();
		cam.dir = d.normalize();

		// get the new up vector
		cam.up = d.cross (up);	// d cross up gives a vector in the plane sort of aligned with the perpendicular of up.
		cam.up = cam.up.cross(d).normalize().mul(up.mag());	// cross again with d to get a vector in the plane aligned with the old up vector
		cam.right = cam.dir.cross (cam.up).normalize().mul(right.mag());	// compute right vector by right-hand rule

		// translation
		cam.bl = bl;
		Float3 proj = cam.project (pt);
		double dx = proj.x - x;
		double dy = proj.y - y;
		cam.bl = cam.right.mul(-0.5).add (cam.up.mul(-0.5)).sub (cam.dir.mul(40));
//		cam.bl = bl.add (cam.right.mul(dx)).add (cam.up.mul(dy));

		cam.scrx = scrx;
		cam.scry = scry;
		System.out.println ("Camera is: bl = " + bl + "; dir = " + dir + "; up = " + up + "; right = " + right);
		return cam;
	}

	public Ray getRay (double x, double y) {	// x in [0..scrx] and y in [0..scry].
		/* This is orthographic */
		x /= scrx;
		y /= scry;
		Float3 start = bl.add (up.mul(y)).add (right.mul(x));
		return new Ray (start, dir);
	}

	public void scale (double amt) {
		Float3 c = bl.add (up.mul (0.5)).add(right.mul(0.5));
		up = up.mul (amt);
		right = right.mul (amt);
		bl = c.sub (up.mul(0.5)).sub(right.mul(0.5));
	}

}
