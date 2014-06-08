package frontend;

public class Range extends Datum {

	public double start, end, step;

	public Range (double start, double end) {
		this.start = start;
		this.end = end;
		step = 1;
	}

	public Range (double start, double end, double step) {
		this (start, end);
		this.step = step;
	}

	public int size () {
		return 1;
	}

	public Datum get (int idx) {
		return this;
	}

	public boolean isTrue () {
		return true;
	}

	public String toString () {
		return "[" + start + ":" + step + ":" + end + "]";
	}
}
