package common ;
public class Transform {

	public Float3 transformPoint (Float3 pt) {
		return null;
	}
	public Float3 transformVec   (Float3 vec) {
		return null;
	}

	public Ray transformRay (Ray r) {
		return new Ray (transformPoint (r.start), transformVec (r.dir));
	}

	private double[][] mat;

	public Transform (double[][] m) {
		mat = m;
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
					m[i][j] += this.mat[i][k] * other.mat[k][j];
				}
			}
		}
		return new Transform (m);
	}

	public String toString () {
		StringBuilder sb = new StringBuilder("[");
		for (int i=0; i<4; i++) {
			sb.append ("[");
			for (int j=0; j<4; j++) {
				sb.append(Float.toString ((float) mat[i][j]) + ((j!=3) ? "," : ""));
			}
			sb.append ("]" + ((i != 3) ? "," : ""));
		}
		sb.append ("]");
		return sb.toString();
	}

}
