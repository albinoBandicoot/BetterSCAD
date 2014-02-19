package frontend;

import java.util.HashMap;
import common.Prefs;
public enum Op {

	ERROR, ADD, SUB, MUL, DIV, MOD, AND, OR, NOT, LT, LE, EQ, NE, GE, GT, HASH, INDEX;	// keep the relationals together with LT as the first and GT as the last, for isRelational to work properly

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

	public boolean okSV () {
		return this != MOD;
	}

	public boolean okVV () {
		return this != MOD && this != DIV;
	}

	public Datum eval (Datum lhs, Datum rhs) {
		if (this == NOT) {
			return new Bool(!lhs.isTrue());
		}
		if (this == SUB && rhs == null) {
			return eval (new Scalar(0), lhs);
		}
		if (this == AND) {
			return new Bool (lhs.isTrue() && rhs.isTrue());
		}
		if (this == OR) {
			return new Bool (lhs.isTrue() || rhs.isTrue());
		}
		if (lhs instanceof Undef || rhs instanceof Undef) {
			if (isRelational()) {
				return new Bool (false);
			} else {
				return new Undef();
			}
		}
		if (this == INDEX) {
			if (lhs instanceof Vec) {
				if (rhs instanceof Scalar) {
					int idx = (int) ((Scalar) rhs).d;
					if (idx < 0 || idx >= lhs.size()) {
						return new Undef();
					} else {
						return lhs.get(idx);
					}
				} else {
					return new Undef();
				}
			} else {
				return new Undef();
			}
		}
		if (lhs instanceof Bool || rhs instanceof Bool) {	// define the relational ops
			if (this == EQ || this == NE) {
				boolean eq;
				if (lhs instanceof Bool && rhs instanceof Bool) {
					eq = lhs.isTrue() == rhs.isTrue();
				} else {
					eq = false;
				}
				return (this == EQ) ? new Bool (eq) : new Bool (!eq);
			} else if (this == LT || this == GT) {	// <, <=, >=, and > are defined rather bizarely in OpenSCAD
				return new Bool (false);
			} else if (this == LE || this == GE) {
				return new Bool (true);
			} else {
				return new Undef();
			}
		}

		if (lhs instanceof Scalar && rhs instanceof Scalar) {
			double a = ((Scalar) lhs).d;
			double b = ((Scalar) rhs).d;
			switch (this) {
				case ADD:
					return new Scalar (a+b);
				case SUB:
					return new Scalar (a-b);
				case MUL:
					return new Scalar (a*b);
				case DIV:	// FIXME: check for b == 0 and do the appropriate thing. Unfortunately,
							// this will probably involve adding another type of Datum for Inf. 
					return new Scalar (a/b);
				case MOD:
					return new Scalar (a%b);	// same deal here
				case LT:
					return new Bool (a < b);
				case LE:
					return new Bool (a <= b);
				case EQ:
					return new Bool (a == b);
				case NE:
					return new Bool (a != b);
				case GE:
					return new Bool (a >= b);
				case GT:
					return new Bool (a > b);
				default:
					return new Undef ();
			}
		} else {
			if (this == MOD) return new Undef();
			if (isRelational()) {
				return new Bool (false);
			}
			boolean both_vecs = lhs instanceof Vec && rhs instanceof Vec;
			if (this == DIV) {
				if (both_vecs) {
					return new Undef();
				} else {
					return evalComponentwise(lhs, rhs, true, true);
				}
			}

			if (this == MUL) {
				if (both_vecs) {
					Vec vl = (Vec) lhs;
					Vec vr = (Vec) rhs;
					if (vl.isFlat() && vr.isFlat()) {
						// dot product.
						return dotProduct (vl, vr);
					} else {
						int lsize = vl.wellFormedMatrix();
						int rsize = vr.wellFormedMatrix();
						if (lsize == -1) {
							if (rsize == -1) {	// at least one is not flat and neither are well formed -> undef
								return new Undef();
							} else {
								if (vl.isFlat()) {
									// row vector * matrix
									if (vl.size() == rsize) {
										Vec res = new Vec ();
										for (int i=0; i < rsize; i++) {
											res.vals.add (dotProduct (vl, vr.getColumn (i)));
										}
										return res;
									} else {
										return new Undef();
									}
								} else {
									return new Undef();
								}
							}
						} else {
							if (rsize == -1) {
								if (vr.isFlat()) {
									// matrix * column vector
									if (vl.size() == vr.size()) {
										Vec res = new Vec ();
										for (int i=0; i < vl.size(); i++) {
											res.vals.add (dotProduct ((Vec) vl.get(i), vr));
										}
										return res;
									} else {
										return new Undef();
									}
								} else {
									return new Undef();
								}
							} else {
								// both are well formed matrices. Now check dimensions (lhs minor = rhs major)
								if (lsize == rhs.size()) {
									Vec mat = new Vec ();
									for (int i=0; i<lsize; i++) {
										mat.vals.add ( eval(vl.get(i), vr) );
									}
									return mat;
								} else {
									return new Undef();
								}
							}
						}
					}
				} else {	// scalar times vector or matrix.
					return evalComponentwise (lhs, rhs, true, true);
				}
			} else {	// add or subtract
				return evalComponentwise(lhs, rhs, false, false);
			}
		}

	}

	private Scalar dotProduct (Vec vl, Vec vr) {
		double sum = 0;
		for (int i=0; i<Math.min(vl.size(), vr.size()); i++) {
			sum += vl.getd (i) * vr.getd (i);
		}
		return new Scalar (sum);
	}

	private int getLen (Datum lhs, Datum rhs, boolean override, boolean override_max) {
		boolean max = override ? override_max : Prefs.current.VECLEN_MAX;
		if (max) {
			return Math.max (lhs.size(), rhs.size());
		} else {
			return Math.min (lhs.size(), rhs.size());
		}
	}

	private Datum evalComponentwise (Datum lhs, Datum rhs, boolean override, boolean override_max) {	// do an operation componentwise.
		int len = getLen (lhs, rhs, override, override_max);
		Vec res = new Vec ();
		for (int i=0; i<len; i++){ 
			res.vals.add (eval (lhs.get(i), rhs.get(i)));
		}
		return res;
	}

}

