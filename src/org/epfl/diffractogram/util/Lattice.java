package org.epfl.diffractogram.util;

import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

@SuppressWarnings("serial")
public class Lattice extends Matrix3d {

	public Matrix3d inverse;

	public double a, b, c;
	public double alpha, beta, gamma; // in degrees !!

	final public Vector3d x, y, z;

	private Vector3d center;
	private Vector3d xhat;
	private Vector3d yhat;
	private Vector3d zhat;

	public Lattice() {
		this(15, 15, 15, 90, 90, 90);
	}

	public Lattice(Lattice l) {
		x = l.x;
		y = l.y;
		z = l.z;
		center = l.center;
		alpha = y.angle(z) * 180d / Math.PI;
		beta = x.angle(z) * 180d / Math.PI;
		gamma = x.angle(y) * 180d / Math.PI;
		a = x.length();
		b = y.length();
		c = z.length();
		init(center);
	}

	/**
	 * from default values
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @param alpha
	 * @param beta
	 * @param gamma
	 * @param u
	 * @param v
	 * @param w
	 */
	public Lattice(double a, double b, double c, double alpha, double beta, double gamma, int u, int v, int w) {
		this(a, b, c, alpha, beta, gamma);
		setOrientation(u, v, w);
	}

	/**
	 * general entrance point
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @param alpha
	 * @param beta
	 * @param gamma
	 */
	public Lattice(double a, double b, double c, double alpha, double beta, double gamma) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
		double y1 = b * cos(gamma);
		double y2 = b * Math.sqrt(1d - cos(alpha) * cos(alpha) - cos(beta) * cos(beta) - cos(gamma) * cos(gamma)
				+ 2d * cos(alpha) * cos(beta) * cos(gamma)) / sin(beta);
		double y3 = b * (cos(alpha) - cos(beta) * cos(gamma)) / sin(beta);
		x = new Vector3d(a, 0, 0);
		y = new Vector3d(y1, y2, y3);
		z = new Vector3d(c * cos(beta), 0, c * sin(beta));
		init(new Vector3d());
		//showLattice();
	}

	/**
	 * for reciprocal
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	private Lattice(Vector3d x, Vector3d y, Vector3d z) {
		alpha = y.angle(z) * 180d / Math.PI;
		beta = x.angle(z) * 180d / Math.PI;
		gamma = x.angle(y) * 180d / Math.PI;
		a = x.length();
		b = y.length();
		c = z.length();
		this.x = x;
		this.y = y;
		this.z = z;
		init(new Vector3d());
	}

	private void init(Vector3d center) {
		if (center != null) {
			this.center = center;
			center.add(x, y);
			center.add(z);
			center.scale(-.5);
		}
		setColumn(0, x);
		setColumn(1, y);
		setColumn(2, z);
		inverse = new Matrix3d(this);
		inverse.invert();
		xhat = new Vector3d(x);
		xhat.normalize();
		yhat = new Vector3d(y);
		yhat.normalize();
		zhat = new Vector3d(z);
		zhat.normalize();
	}

//	private void showLattice() {
//		Point3d o = new Point3d(0, 0, 0);
//		Point3d a = new Point3d(1, 0, 0);
//		Point3d b = new Point3d(0, 1, 0);
//		Point3d c = new Point3d(0, 0, 01);
//	}

	public void setOrientation(int u, int v, int w) {
		Vector3d e2 = new Vector3d(0, 1, 0);
		Vector3d p = new Vector3d(u * x.getX() + v * y.getX() + w * z.getX(),
				u * x.getY() + v * y.getY() + w * z.getY(), u * x.getZ() + v * y.getZ() + w * z.getZ());
		double angle = p.angle(e2);
		Vector3d n = new Vector3d();
		n.cross(e2, p);
		Matrix3d r = Utils3d.getRotationMatrix(angle, n);
		Transform3D t3d = new Transform3D();
		t3d.set(r);

		t3d.transform(x);
		t3d.transform(y);
		t3d.transform(z);
		init(new Vector3d());
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (a == b && b == c)
			sb.append("a,b,c=" + a);
		else if (a == b)
			sb.append("a,b=" + a + " c=" + c);
		else if (a == c)
			sb.append("a,c=" + a + " b=" + b);
		else if (b == c)
			sb.append("a=" + a + " b,c=" + b);
		else
			sb.append("a=" + a + " b=" + b + " c=" + c);

		// if (alpha!=90) {
		sb.append(" \u03b1");
		if (beta == alpha)
			sb.append(",\u03b2");
		if (gamma == alpha)
			sb.append(",\u03b3");
		sb.append("=" + (int) alpha);
		// }
		if (// beta!=90 &&
		beta != alpha) {
			sb.append(" \u03b2");
			if (gamma == beta)
				sb.append(",\u03b3");
			sb.append("=" + (int) beta);
		}
		if (// gamma!=90 &&
		gamma != alpha && gamma != beta) {
			sb.append(" \u03b3");
			sb.append("=" + (int) gamma);
		}
		return sb.toString();
		// return "a="+a+" b="+b+" c="+c+" alpha="+alpha+" beta="+beta+" gamma="+gamma;
	}

	public double volume() {
		return a * b * c
				* Math.sqrt(1 - cos2(alpha) - cos2(beta) - cos2(gamma) + 2 * cos(alpha) * cos(beta) * cos(gamma));
	}

	public static double volume(Vector3d x, Vector3d y, Vector3d z) {
		Vector3d v = new Vector3d();
		v.cross(y, z);
		return v.dot(x);
	}

	public Lattice reciprocal() {
		Vector3d[] vv = reciprocal(x, y, z);
		return new Lattice(vv[0], vv[1], vv[2]);
	}

	public static Vector3d[] reciprocal(Vector3d x, Vector3d y, Vector3d z) {
		double iv = 1d / volume(x, y, z);
		Vector3d[] r = new Vector3d[3];
		r[0] = new Vector3d();
		r[1] = new Vector3d();
		r[2] = new Vector3d();
		r[0].cross(y, z);
		r[1].cross(z, x);
		r[2].cross(x, y);
		r[0].scale(iv);
		r[1].scale(iv);
		r[2].scale(iv);
		// note that these lattice vector lengths
		// are all >= 1/a, 1/b, and 1/c. 
//		System.out.println("latt " + r[0].length() * x.length());
//		System.out.println("latt " + r[1].length() * y.length());
//		System.out.println("latt " + r[2].length() * z.length());
		// beta = 120
		// latt 1.1547005383792512
		// latt 0.9999999999999999
		// latt 1.1547005383792515
		//
		// this is becuase they must project onto the
		// a, b, and c axes to give the necessary 
		// n lambda / a distance.
				return r;
	}

	private static double sin(double a) { // arguments in degrees !!
		return Math.sin(a * Math.PI / 180d);
	}

	private static double cos(double a) {
		return Math.cos(a * Math.PI / 180d);
	}

//	private static double sin2(double a) {
//		return Math.pow(sin(a), 2);
//	}
	private static double cos2(double a) {
		return Math.pow(cos(a), 2);
	}

	public static Vector3d round(Vector3d p) {
		if (Double.isNaN(p.getX()) || Double.isInfinite(p.getX()) || Double.isNaN(p.getY())
				|| Double.isInfinite(p.getY()) || Double.isNaN(p.getZ()) || Double.isInfinite(p.getZ()))
			return p;
		return new Vector3d(Math.round(1000 * p.getX()) / 1000d, Math.round(1000 * p.getY()) / 1000d,
				Math.round(1000 * p.getZ()) / 1000d);
	}

	public Point3d transform(Point3d p) {
		super.transform(p);
		p.add(center);
		return p;
	}

	public Tuple3d rotate(Tuple3d p) {
		super.transform(p);
		return p;
	}

	public void invertTransform(Tuple3d v) {
		inverse.transform(v);
	}

	public boolean project(Tuple3d t, char axis, Vector3d vout) {
		Vector3d v = (axis == 'x' ? xhat : axis == 'y' ? yhat : zhat);
		double dot = t.x * v.x + t.y * v.y + t.z * v.z;
		if (Math.abs(dot) < 1e-6)
			return false;

		return true;

	}

}
