package frontend;
public class FileMark {

	public int line;
	public int col;
	public int idx;	// character index in file, starting at 0.

	public FileMark (int line, int col, int idx) {
		this.line = line;
		this.col = col;
		this.idx = idx;
	}

	public String toString () {
		if (line == -1) return "EOF";
		return line + ":" + col;
	}
}
