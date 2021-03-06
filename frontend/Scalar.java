package frontend;

public class Scalar extends Datum {

	public double d;

	public Scalar () {
		d = 0;
	}

	public Scalar (double x) {
		d = x;
	}

	public int size () {
		return 1;
	}

	public Datum get (int idx) {
		if (idx == 0)  return this;
		return new Undef();
	}

	public boolean isTrue () {
		return d != 0.0;
	}

	public String toString () {
		if (d == (int) d) {
			return Integer.toString ((int) d);
		}
		return Float.toString ((float) d);
	}
}


