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

		Node[][] objs = new Node[ys][xs];

		for (int x=0; x<xs; x++) {
			for (int y=0; y<ys; y++) {
				Ray r = s.cam.getRay (x, y);
				Intersection i = s.intersection (r);
				int loc = y*xs + x;
//				System.out.println ("Ipt t = " + (i == Intersection.NONE ? "X" : i.t));
				if (i == Intersection.NONE) {
					// background
					data[loc] = bkgr;
				} else {
					// get the normal. This is just the gradient of the csg function, since
					// the normal to a level set is the gradient of the function.
					// This gradient is computed here by the secant method.
					Float3 ipt = r.get (i.t);
					double fpt = s.root.csg (ipt);
					double fdx = s.root.csg (ipt.add (new Float3 (STEP, 0, 0))) - fpt;
					double fdy = s.root.csg (ipt.add (new Float3 (0, STEP, 0))) - fpt;
					double fdz = s.root.csg (ipt.add (new Float3 (0, 0, STEP))) - fpt;
					norm_csgct += 4;
					Float3 norm = new Float3 (fdx, fdy, fdz).normalize();
		//			System.out.println ("(x, y) = (" + x + ", " + y + "); ipoint = " + ipt + "; csg = " + fpt);
					Float3 light = Prefs.current.AMBIENT;
					for (Light l : s.lights) {
						light = light.add (l.getContrib (ipt, r.dir, norm));
					}
					Float3 col = i.obj.mat.col.mul (light);

					data[loc] = col.clamp().getImgRGB();
					objs[y][x] = i.obj;
				}
			}
		}

		for (int x=1; x<xs-1; x++) {
			for (int y=1; y<ys-1; y++) {
				int loc = y*xs + x;
				Node n = objs[y][x];
				if (n != objs[y-1][x] || n != objs[y+1][x] || n != objs[y][x-1] || n != objs[y][x+1]) {
					data[loc] = 0;
				}
			}
		}
		System.out.println ("Intersection cull csg count: " + s.il.csg_ct + "; normal csg count: " + norm_csgct + "; TOTAL = " + (s.il.csg_ct + norm_csgct));
		return System.currentTimeMillis() - time;
	}
	
}
