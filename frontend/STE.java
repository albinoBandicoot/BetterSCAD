package frontend;

public class STE implements Comparable<STE> {

	public Tree t;
	public Datum d;

	public STE (Tree t) {
		this.t = t;
		d = null;
	}

	public STE (Tree t, Datum d) {
		this.t = t;
		this.d = d;
	}

	public int compareTo (STE other) {
		if (t.id == other.t.id) return 0;	// should never happen
		if (t.id < other.t.id) return -1;
		return 1;
	}

}
