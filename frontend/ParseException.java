package frontend;
public class ParseException extends Exception {

	public FileMark fm;

	public ParseException (String s, FileMark fm) {
		super (s);
		this.fm = fm;
	}
}
