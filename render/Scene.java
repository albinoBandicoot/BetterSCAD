package render;
import common.*;
import java.util.ArrayList;
public class Scene {

	public Camera cam;
	public ArrayList <Light> lights;
	public Node root;
	public Float3 bkgr_col;
	public IList il;

	public Scene (Node r) {
		root = r;
		il = new IList (root);
		cam = new Camera();
		lights = new ArrayList<Light>();
		bkgr_col = new Float3 (0.85, 0.95, 0.9);
		setDefaultLights ();
	}

	public Scene (Camera c, Node root, ArrayList<Light> lights) {
		this.cam = c;
		this.root = root;
		this.il = new IList (root);
		this.lights = lights;
		this.bkgr_col = new Float3 (0.85, 0.95, 0.9);
	}

	public void rotateStuff (double phi, double theta) {	// rotate camera and lights
		Float3 newdir = cam.dir.axisRotate (cam.right.normalize(), phi);
		newdir = newdir.axisRotate (cam.up.normalize(), -theta);
		cam = cam.lookAt (new Float3(0,0,0), newdir);

		for (Light light : lights) {
			light.dir = light.dir.axisRotate (cam.right.normalize(), phi);
			light.dir = light.dir.axisRotate (cam.up.normalize(), -theta);
		}
	}

	public static double STEPSIZE_MIN = 0.05;
	public static double MAX_DERIV_FAC = 10;
	public static double MINISTEP = STEPSIZE_MIN/20;
	public static double FAR_PLANE = 100;

	public Intersection intersection (Ray r) {
		return root.intersection (il, r);
		
		/*
		double t = 0;
		double STMIN = STEPSIZE_MIN;// * (0.8 + 0.4*Math.random());
		int i = 0;
		while (t < FAR_PLANE) {
			i++;
			double d = root.dist (r.get(t));
			if (Math.abs (d) < STMIN) {
				il.csg_ct += i;
				return new Intersection (t, null);
			}
			i++;
			double e = root.dist (r.get(t + MINISTEP));
			double deriv = (e-d)/MINISTEP;
			if (deriv > 0) {	// distance increasing
				t += Math.max (e, STMIN);
			} else {
				if (deriv < -1) {
					System.out.println ("INCONSISTENT DISTANCE!");
				}
				double dfac = Math.min(-1/deriv, MAX_DERIV_FAC);
				t += Math.max (d*dfac, STMIN);
			}
		}
		il.csg_ct += i;
		return Intersection.NONE;
		*/
	}

		
	public void updateCameraSize (int xs, int ys) {
		double rx = ((double) xs)/cam.scrx;
		double ry = ((double) ys)/cam.scry;

		Float3 cent = cam.bl.add(cam.right.mul(0.5)).add(cam.up.mul(0.5));
		cam.right = cam.right.mul(rx);
		cam.up = cam.up.mul(ry);
		cam.bl = cent.sub(cam.right.mul(0.5)).sub(cam.up.mul(0.5));
		cam.scrx = xs;
		cam.scry = ys;

	}

	public void setDefaultLights () {
		lights.clear();
		lights.add (new Light (new Float3 (1,1,1), new Float3 (0.5, 0.4, 0.3)));
		lights.add (new Light (new Float3 (-1, -1, 1), new Float3 (0.3, 0.5, 0.4)));
		lights.add (new Light (new Float3 (0, 1, 0), new Float3 (0.4, 0.3, 0.5)));
	}
}
