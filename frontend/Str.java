package frontend;

public class Str extends Datum {

	public String data;

	public Datum get (int idx) {
		// what should this do?
		return this;
//		return new Undef ();
	}

	public String toString () {
		return data;
	}
}
