package render ;
import common.*;
import java.awt.*;
import java.awt.image.*;
public class  Raytrace {

	public static final double STEP = 0.0001;


	public static long render (Scene s, int[] data, int xs, int ys) {
		long time = System.currentTimeMillis();
		int bkgr = s.bkgr_col.getImgRGB();

		System.out.println ("Will trace " + xs + " x " + ys + " (" + (xs * ys) + " px)");
		int norm_csgct = 0;
		s.il.csg_ct = 0;
		for (int x=0; x<xs; x++) {
			for (int y=0; y<ys; y++) {
				Ray r = s.cam.getRay (x, y);
				Intersection i = s.intersection (r);
				int loc = y*xs + x;
				if (i == Intersection.NONE) {
					// background
					data[loc] = bkgr;
				} else {
					// get the normal. This is just the gradient of the csg function, since
					// the normal to a level set is the gradient of the function.
					// This gradient is computed here by the secant method.
					Float3 ipt = r.get (i.t);
					double fpt = s.root.csg (ipt);
					double fdx = s.root.csg (ipt.add (new Float3 (STEP, 0, 0)));
					double fdy = s.root.csg (ipt.add (new Float3 (0, STEP, 0)));
					double fdz = s.root.csg (ipt.add (new Float3 (0, 0, STEP)));
					norm_csgct += 4;
					Float3 norm = new Float3 (fdx, fdy, fdz).normalize();
		//			System.out.println ("(x, y) = (" + x + ", " + y + "); ipoint = " + ipt + "; csg = " + fpt);
					Float3 light = new Float3(0,0,0); //i.flag ? new Float3(0.3,0.0,0.0) : new Float3(0.0,0.3,0.0); //new Float3 (0.3, 0.3, 0.3);	// ambient term
					for (Light l : s.lights) {
						light = light.add (l.getContrib (ipt, r.dir, norm));
					}
					Float3 col = i.obj.mat.col.mul (light);

					data[loc] = col.clamp().getImgRGB();
				}
			}
		}
		System.out.println ("Intersection cull csg count: " + s.il.csg_ct + "; normal csg count: " + norm_csgct + "; TOTAL = " + (s.il.csg_ct + norm_csgct));
		return System.currentTimeMillis() - time;
	}
	
}
