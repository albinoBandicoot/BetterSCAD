package frontend;

import java.util.ArrayList;
import common.Float3;
public class Vec extends Datum {

	public ArrayList<Datum> vals;

	public Vec (ArrayList<Datum> vals) {
		this.vals = vals;
	}

	public Vec () {
		vals = new ArrayList<Datum>();
	}

	public Vec (double x, double y, double z) {
		this();
		vals.add (new Scalar(x));
		vals.add (new Scalar(y));
		vals.add (new Scalar(z));
	}

	public Vec (Vec r1, Vec r2, Vec r3) {	// convenience method for constructing matrices
		this();
		vals.add (r1);
		vals.add (r2);
		vals.add (r3);
	}

	public int size () {
		return vals.size();
	}

	public boolean isFlat () {
		for (Datum d : vals) {
			if (! (d instanceof Scalar)) return false;
		}
		return true;
	}

	public int wellFormedMatrix () {	// -1 if not well formed, otherwise the minor length
		int minor_len = -1;
		for (Datum d : vals) {
			if (!(d instanceof Vec)) return -1;
			Vec v = (Vec) d;
			if (!v.isFlat()) return -1;
			if (minor_len == -1) {
				minor_len = v.size();
			} else {
				if (v.size() != minor_len) return -1;
			}
		}
		return minor_len;
	}

	public Vec getColumn (int j) {
		Vec col = new Vec ();
		for (int i=0; i<vals.size(); i++) {
			col.vals.add (vals.get(i).get(j));
		}
		return col;
	}

	public Datum get (int i) {
		if (i >= 0 && i < vals.size()) {
			return vals.get(i);
		}
		return new Scalar(0);
	}

	public double getd (int i) {
		if (i >= 0 && i < vals.size()) {
			if (vals.get(i) instanceof Scalar) {
				return ((Scalar) vals.get(i)).d;
			}
		}
		return 0;
	}

	public Float3 getFloat3 () {
		return new Float3 (getd(0), getd(1), getd(2));
	}

	public boolean isTrue () {
		return !vals.isEmpty();	// this is how OpenSCAD seems to interpret it
	}

	public String toString () {
		StringBuilder sb = new StringBuilder ();
		sb.append ("[");
		for (Datum d : vals) {
			sb.append (d.toString() + ((d == vals.get(vals.size()-1)) ? "" : ", "));
		}
		sb.append ("]");
		return sb.toString();
	}
}
