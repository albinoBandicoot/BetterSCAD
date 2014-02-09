import java.util.ArrayList;
public class Vector extends Datum {

	public ArrayList<Datum> vals;

	public Vector (ArrayList<Datum> vals) {
		this.vals = vals;
	}

	public Vector () {
		vals = new ArrayList<Datum>();
	}

	public Datum get (int i) {
		return vals.get(i);
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
