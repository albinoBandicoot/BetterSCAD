package common ;
public class Transform {

	public Float3 transformPoint (Float3 pt) ;
	public Float3 transformVec   (Float3 vec);

	private double[][] mat;

	private Transform (double[][] m) {
		mat = new double[4][4];

	}

	public Transform () {
		mat = identity ();
	}

	private static double[][] identity () {
		double[][] m = new double[4][4];
		for (int i=0; i<4; i++) {
			m[i][i] = 1;
		}
		return m;
	}

	public static Transform makeScale (Float3 v) {
		double[][] m = identity ();
		m[0][0] = v.x;
		m[1][1] = v.y;
		m[2][2] = v.z;
		return new Transform (m);
	}

	public static Transform makeTranslate (Float3 v) {
		double[][] m = identity ();
		m[0][3] = v.x;
		m[1][3] = v.y;
		m[2][3] = v.z;
		return new Transform (m);
	}

	public static Transform makeRotate (Float3 r, double t) {	// rotate about arbitrary axis r by t radians
		r = r.normalize();
		/* See page 71 of Real-Time Rendering by Moeller, Haines, and Hoffman. */
		double[][] m = identity();
		double cos = Math.cos(t);
		double sin = Math.sin(t);
		double omc = 1 - cos;

		m[0][0] = cos + omc*r.x*r.x;
		m[0][1] = omc*r.x*r.y - r.z*sin;
		m[0][2] = omc*r.x*r.z + r.y*sin;
		m[1][0] = omc*r.x*r.y + r.z*sin;
		m[1][1] = cos + omc*r.y*r.y;
		m[1][2] = omc*r.y*r.z - r.x*sin;
		m[2][0] = omc*r.x*r.z - r.y*sin;
		m[2][1] = omc*r.y*r.z + r.x*sin;
		m[2][2] = cos + omc*r.z*r.z;

		return new Transform (m);
	}

	public Transform append (Transform other) {
		double[][] m = new double[4][4];
		for (int i=0; i<4; i++) {
			for (int j=0; j<4; j++) {
				for (int k=0; k<4; k++) {
					m[i][j] += this.m[i][k] * other.m[k][j];
				}
			}
		}
		return new Transform (m);
	}
}
