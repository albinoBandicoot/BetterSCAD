package common;
import java.awt.Font;
public class Prefs {


	public static final int ALL = 2;
	public static final int PREDEFS = 1;
	public static final int NONE = 0;

	public boolean 	RUNTIME_VARS = true;
	public boolean 	VECLEN_MAX = false;
	public int 		PROTECT_FROM_UNDEF = PREDEFS;	// whether to overwrite default for a parameter if undef is passed in
	public int 		STACK_HEIGHT_CAP = 50;

	/* GUI OPTIONS */
	public double	PHI_SCALE = -50.0;
	public double	THETA_SCALE = 36.0;
	public double	SCALE_BASE = 1.18;	// multiplier for zooming, per wheel click
	public Float3	DEFAULT_OBJ_COLOR = new Float3 (1.0, 1.0, 1.0);

	public Font		EDITOR_FONT = new Font ("Courier", Font.PLAIN, 13);
	public int		EDITOR_TAB_SIZE = 6;

	/* RENDERING OPTIONS */
	public Float3	AMBIENT = new Float3 (0.3, 0.3, 0.3);
	public int		DOWNSAMPLING = 3;

	public static Prefs current = new Prefs();

	public Prefs () {	// defaults correspond to OpenSCAD.
	}
}
