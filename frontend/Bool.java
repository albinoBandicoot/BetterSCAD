package frontend;

public class Bool extends Datum {

	public boolean val;

	public Bool (boolean v) {
		val = v;
	}

	public Bool () {
		val = false;
	}

	public int size () {
		return 1;
	}

	public Datum get (int i) {
		return this;
	}

	public String toString () {
		return val ? "true" : "false";
	}

	public boolean isTrue () {
		return val;
	}
}


