package frontend;

public class Str extends Datum {

	public String data;

	public Str (String d) {
		this.data = d;
	}

	public Datum get (int idx) {
		if (idx >= 0 && idx < data.length()) {
			return new Str (Character.toString (data.charAt(idx)));
		} else {
			return new Undef();
		}
	}

	public int size () {
		return data.length();
	}

	public boolean isTrue () {
		return size() > 0;
	}

	public String toString () {
		return data;
	}
}
