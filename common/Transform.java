package common ;
public class Transform {

	public Float3 transformPoint (Float3 pt) {
		return new Float3 (mat[0][0]*pt.x + mat[0][1]*pt.y + mat[0][2]*pt.z + mat[0][3],
						   mat[1][0]*pt.x + mat[1][1]*pt.y + mat[1][2]*pt.z + mat[1][3],
						   mat[2][0]*pt.x + mat[2][1]*pt.y + mat[2][2]*pt.z + mat[2][3]);
	}
	public Float3 transformVec   (Float3 vec) {
		return new Float3 (mat[0][0]*vec.x + mat[0][1]*vec.y + mat[0][2]*vec.z,
						   mat[1][0]*vec.x + mat[1][1]*vec.y + mat[1][2]*vec.z,
						   mat[2][0]*vec.x + mat[2][1]*vec.y + mat[2][2]*vec.z);
	}

	public Ray transformRay (Ray r) {
		Ray res = new Ray (transformPoint (r.start), transformVec (r.dir));
//		System.out.println ("Transforming " + r + " to " + res);
		return res;
	}

	private double[][] mat;

	public Transform (double[][] m) {
		mat = m;
	}

	public Transform () {
		mat = identity ();
	}

	public Transform (Transform other) {
		mat = new double[4][4];
		for (int i=0; i<4; i++){
			for (int j=0; j<4; j++) {
				mat[i][j] = other.mat[i][j];
			}
		}
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

	public static Transform makeRotate (Float3 r, double t) {	// rotate about arbitrary axis r by t degrees
		// first convert to radians
		t *= Math.PI/180;
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

	private void prmat (double[][] m) {
		for (int i=0; i<4; i++) {
			for (int j=0; j<8; j++) {
				System.out.print (m[i][j] + "  ");
			}
			System.out.println();
		}
	}

	public Transform invert () {
		double[][] m = new double[4][8];
		for (int i=0; i<4; i++) {
			for (int j=0; j<8; j++) {
				if (j < 4) {
					m[i][j] = this.mat[i][j];
				} else {
					m[i][j] = (i == (j-4)) ? 1 : 0;
				}
			}
		}
		// Gaussian elimination:
		// forward direction
		for (int col = 0; col < 4; col ++) {
			for (int row = col+1; row < 4; row++) {
				double scale = -m[row][col] / m[col][col];
				// add row 'col' multiplied by scale to this row, cancelling out the leading entry.
				for (int x=0; x<8; x++) {
					m[row][x] += scale*m[col][x];
				}
			}
			// scale row so it has a leading 1
			double sc = 1.0/m[col][col];
			for (int x=0; x<8; x++) {
				m[col][x] *= sc;
			}
		}
		// backward direction
		for (int c=3; c>=0; c--) {
			for (int r = c-1; r >= 0; r--) {
				double scale = -m[r][c];
				// add row 'c' multiplied by scale to row 'r'
				for (int x=0; x<8; x++) {
					m[r][x] += scale*m[c][x];
				}
			}
		}
		// copy over the right hand 4x4 submatrix
		double[][] dat = new double[4][4];
		double[][] CHECK = new double[4][4];	// this should end up being the identity matrix
		for (int i=0; i<4; i++) {
			for (int j=0; j<4; j++) {
				dat[i][j] = m[i][j+4];
				CHECK[i][j] = m[i][j];
			}
		}
		System.out.println ("INVLHS = " + (new Transform (CHECK)).toString());
		return new Transform (dat);
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
