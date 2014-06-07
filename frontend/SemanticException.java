package frontend;
public class SemanticException extends Exception {

	public FileMark fm;

	public SemanticException (String s) {
		super(s);
	}

	public SemanticException (String s, Tree t) {
		super(s);
		this.fm = t.fm;
	}
}
