package frontend;

public class Undef extends Datum {

	public Undef () {
	}

	public int size () {
		return 0;
	}

	public Datum get (int i) {
		return this;
	}

	public boolean isTrue () {
		return false;
	}

	public String toString () {
		return "undef";
	}


}
