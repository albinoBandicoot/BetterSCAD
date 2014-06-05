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


	public Intersection intersection (Ray r) {
		return root.intersection (il, r);
	}

	public void updateCameraSize (int xs, int ys) {
		cam = new Camera (xs, ys);
	}

	public void setDefaultLights () {
		lights.clear();
		lights.add (new Light (new Float3 (1,1,1), new Float3 (0.8, 0.6, 0.5)));
		lights.add (new Light (new Float3 (-1, -1, 1), new Float3 (0.6, 0.8, 0.7)));
		lights.add (new Light (new Float3 (0, 1, 0), new Float3 (0.4, 0.3, 0.8)));
	}
}
