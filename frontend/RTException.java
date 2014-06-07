package frontend;
public class RTException extends Exception {

	public FileMark fm;

	public RTException (String s) {
		super (s);
	}

	public RTException (String s, Tree t) {
		super (s);
		fm = t.fm;
	}
}
