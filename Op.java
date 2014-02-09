import java.util.HashMap;
public enum Op {

	ERROR, ADD, SUB, MUL, DIV, MOD, AND, OR, NOT, LT, LE, EQ, NE, GE, GT, HASH;	// keep the relationals together with LT as the first and GT as the last, for isRelational to work properly

	public static final HashMap <String, Op> ops = new HashMap <String, Op> ();

	static {
		ops.put ("+",   ADD);
		ops.put ("-",   SUB);
		ops.put ("*",   MUL);
		ops.put ("/", 	DIV);
		ops.put ("%", 	MOD);
		ops.put ("&&",	AND);
		ops.put ("||",	OR);
		ops.put ("!", 	NOT);
		ops.put ("<", 	LT );
		ops.put ("<=",	LE );
		ops.put ("==", 	EQ );
		ops.put ("!=", 	NE );
		ops.put (">=", 	GE );
		ops.put (">", 	GT );
		ops.put ("#",	HASH);
	}

	public static Op get (String s) {
		if (ops.containsKey (s)) {
			return ops.get (s);
		}
		return ERROR;
	}

	public boolean isRelational () {
		return this.ordinal() >= LT.ordinal() && this.ordinal() <= GT.ordinal();
	}

	public boolean isTreemod () {
		return this == HASH || this == NOT || this == MUL || this == MOD;
	}
}
