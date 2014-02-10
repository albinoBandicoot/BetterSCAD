package frontend;

public class Token {

	public Tokentype type;
	public String val;
	public double nval;
	public int line;

	public Token (Tokentype type, int line) {
		this.type = type;
		this.val = "";
		this.nval = 0;
		this.line = line;
	}

	public Token (Tokentype type, String val, int line) {
		this.type = type;
		this.val = val;
		this.nval = 0;
		this.line = line;
	}

	public Token (Tokentype type, double nval, int line) {
		this.type = type;
		this.val = "";
		this.nval = nval;
		this.line = line;
	}

	public boolean is (String s) {
		return type == Tokentype.KEYWORD && val.equals(s);
	}

	public boolean isOp (String s) {
		if (type == Tokentype.OP) {
			if (!Op.get(s).equals(Op.ERROR)) {
				return Op.get(val).equals(Op.get(s));
			}
		}
		return false;
	}

	public boolean isOp (Op x) {
		if (type == Tokentype.OP) {
			return Op.get(val).equals(x);
		}
		return false;
	}

	public Op getOp () {
		if (type == Tokentype.OP) {
			return Op.get (val);
		}
		return Op.ERROR;
	}

	public String toString () {
		return type.name() + "  " + val + ((nval == 0) ? "" : nval);
	}
}
