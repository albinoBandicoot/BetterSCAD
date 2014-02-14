package frontend;

public class Scalar extends Datum {

	public double d;

	public Scalar () {
		d = 0;
	}

	public Scalar (double x) {
		d = x;
	}

	public Datum get (int idx) {
		if (idx == 0)  return this;
		return new Undef();
	}

	public boolean isTrue () {
		return d != 0.0;
	}

	public String toString () {
		return Float.toString ((float) d);
	}
}


