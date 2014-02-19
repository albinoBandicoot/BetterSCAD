package common;
public class Prefs {


	public static final int ALL = 2;
	public static final int PREDEFS = 1;
	public static final int NONE = 0;

	public boolean 	RUNTIME_VARS = false;
	public boolean 	VECLEN_MAX = false;
	public int 		PROTECT_FROM_UNDEF = PREDEFS;	// whether to overwrite default for a parameter if undef is passed in
	public int 		STACK_HEIGHT_CAP = 50;

	public static Prefs current = new Prefs();

	public Prefs () {	// defaults correspond to OpenSCAD.
	}
}
