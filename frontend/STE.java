package frontend;

public class STE {

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

}
