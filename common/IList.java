package common;
import java.util.Arrays;

public class IList {

	public Intersection[] ints;
	public int n;
	public int csg_ct;	// diagnostics

	public IList (int cap) {
		ints = new Intersection[cap];
		n = 0;
	}

	public IList (Node root) {
		ints = new Intersection[root.findIptsMax()];
		System.out.println ("Created IList with capacity " + ints.length);
		n = 0;
	}

	public void clear () {
		n = 0;
		for (int i=0; i<ints.length; i++) {
			ints[i] = null;
		}
	}

	public void add (Intersection i) {
		ints[n++] = i;
	}

	public int countNonNull () {
		int ct = 0;
		for (int i=0; i<n; i++) {
			if (ints[i] != null) ct++;
		}
		return ct;
	}

	public Intersection[] getSorted () {	// gets the non-null entries upto n, sorted on t-value
		int ct = countNonNull();
		Intersection[] res = new Intersection[ct];
		int w = 0;
		for (int i=0; i<n; i++) {
			if (ints[i] != null) res[w++] = ints[i];
		}
		Arrays.sort(res);
		return res;
	}

}
