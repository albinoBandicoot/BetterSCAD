package frontend;

public abstract class  Datum {

	public abstract Datum get (int idx);

	public abstract String toString ();

	public final boolean isUndef () {
		return this instanceof Undef;
	}

	public abstract boolean isTrue ();	// so they can all be interpreted as conditions

}
