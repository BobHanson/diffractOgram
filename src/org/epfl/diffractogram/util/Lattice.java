package org.epfl.diffractogram.util;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.media.j3d.Transform3D;

public class Lattice {
	public double a, b, c;
	public double alpha, beta, gamma;		// in degrees !!
	public Vector3d x, y, z;
	public Matrix3d m;
	public Vector3d o;
	
	public Lattice() {
		this(5, 5, 5, 90, 90, 90);
	}
	
	public Lattice(double a, double b, double c, double alpha, double beta, double gamma) {
		this.a=a; this.b=b; this.c=c; this.alpha=alpha; this.beta=beta; this.gamma=gamma;     
		double y1 = b*cos(gamma);
		double y2 = b*Math.sqrt(1d-cos(alpha)*cos(alpha)-cos(beta)*cos(beta)-cos(gamma)*cos(gamma)+2d*cos(alpha)*cos(beta)*cos(gamma))/sin(beta);
		double y3 = b*(cos(alpha)-cos(beta)*cos(gamma))/sin(beta);
		x = new Vector3d(a, 0, 0);
		y = new Vector3d(y1, y2, y3);
		z = new Vector3d(c*cos(beta), 0, c*sin(beta));
		o = new Vector3d(); 
		o.add(x, y);
		o.add(z);
		o.scale(-.5);
		m = new Matrix3d();
		m.setColumn(0, x);
		m.setColumn(1, y);
		m.setColumn(2, z);
	}

	public Lattice(Vector3d x, Vector3d y, Vector3d z, Vector3d o) {
		this.x=x; this.y=y; this.z=z; this.o=o;
		alpha = y.angle(z)*180d/Math.PI;
		beta = x.angle(z)*180d/Math.PI;
		gamma = x.angle(y)*180d/Math.PI;
		a = x.length();
		b = y.length();
		c = z.length();
		m = new Matrix3d();
		m.setColumn(0, x);
		m.setColumn(1, y);
		m.setColumn(2, z);
	}
	
	public Lattice(Vector3d x, Vector3d y, Vector3d z) {
		this.x=x; this.y=y; this.z=z;
		alpha = y.angle(z)*180d/Math.PI;
		beta = x.angle(z)*180d/Math.PI;
		gamma = x.angle(y)*180d/Math.PI;
		a = x.length();
		b = y.length();
		c = z.length();
		o = new Vector3d(); 
		o.add(x, y);
		o.add(z);
		o.scale(-.5);
		m = new Matrix3d();
		m.setColumn(0, x);
		m.setColumn(1, y);
		m.setColumn(2, z);
	}
	
	public Lattice(Lattice l) {
		this(l.x, l.y, l.z, l.o);
	}

	public Lattice(double a, double b, double c, double alpha, double beta, double gamma, int u, int v, int w) {
		this(a, b, c, alpha, beta, gamma);
		setOrientation(u, v, w);
	}
	
	public void setOrientation(int u, int v, int w) {
		Vector3d e2 = new Vector3d(0,1,0);
		Vector3d p = new Vector3d(
				u*x.getX()+v*y.getX()+w*z.getX(), 
				u*x.getY()+v*y.getY()+w*z.getY(), 
				u*x.getZ()+v*y.getZ()+w*z.getZ());
		double angle = p.angle(e2);
		Vector3d n = new Vector3d();
		n.cross(e2, p);
		Matrix3d r = rotationMatrix(angle, n);
		Transform3D t3d = new Transform3D();
		t3d.set(r);
		
		t3d.transform(x);
		t3d.transform(y);
		t3d.transform(z);
		
		o = new Vector3d(); 
		o.add(x, y);
		o.add(z);
		o.scale(-.5);
		m = new Matrix3d();
		m.setColumn(0, x);
		m.setColumn(1, y);
		m.setColumn(2, z);
	}
	
	public static Matrix3d rotationMatrix(double angle, Vector3d v) {
    double c = Math.cos(angle);
    double s = Math.sin(angle);
    double t = 1-c;
    double n = v.length();
    double vx = v.getX() / n;
    double vy = v.getY() / n;
    double vz = v.getZ() / n;
    Matrix3d m = new Matrix3d();
    m.m00 = t * vx * vx + c;
    m.m01 = t * vy * vx + s * vz;
    m.m02 = t * vz * vx - s * vy;
    m.m10 = t * vx * vy - s * vz;
    m.m11 = t * vy * vy + c;
    m.m12 = t * vz * vy + s * vx;
    m.m20 = t * vx * vz + s * vy;
    m.m21 = t * vy * vz - s * vx;
    m.m22 = t * vz * vz + c;
    return m;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (a==b && b==c) sb.append("a,b,c="+a);
		else if (a==b) sb.append("a,b="+a+" c="+c);
		else if (a==c) sb.append("a,c="+a+" b="+b);
		else if (b==c) sb.append("a="+a+" b,c="+b);
		else sb.append("a="+a+" b="+b+" c="+c);
		
		if (alpha!=90) {
			sb.append(" \u03b1");
			if (beta==alpha) sb.append(",\u03b2");
			if (gamma==alpha) sb.append(",\u03b3");
			sb.append("="+(int)alpha);
		}
		if (beta!=90 && beta!=alpha) {
			sb.append(" \u03b2");
			if (gamma==beta) sb.append(",\u03b3");
			sb.append("="+(int)beta);
		}
		if (gamma!=90 && gamma!=alpha && gamma!=beta) {
			sb.append(" \u03b3");
			sb.append("="+(int)gamma);
		}
		return sb.toString();
		//return "a="+a+" b="+b+" c="+c+" alpha="+alpha+" beta="+beta+" gamma="+gamma;
	}

	public double volume() {
		return a*b*c*Math.sqrt(1-cos2(alpha)-cos2(beta)-cos2(gamma)+2*cos(alpha)*cos(beta)*cos(gamma));
	}

	
	public static double volume(Vector3d x, Vector3d y, Vector3d z) {
		Vector3d v = new Vector3d ();
		v.cross(y, z);
		return v.dot(x);
	}
	
	public Lattice reciprocal() {
		Vector3d[] vv = reciprocal(x, y, z);
		return new Lattice(vv[0], vv[1], vv[2]);
	}

	public static Vector3d[] reciprocal(Vector3d x, Vector3d y, Vector3d z) {
		double iv = 1d/volume(x, y, z);
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
		return r;
	}
	
	private static double sin(double a) {		// arguments in degrees !!
		return Math.sin(a*Math.PI/180d);
	}
	private static double cos(double a) {
		return Math.cos(a*Math.PI/180d);
	}
//	private static double sin2(double a) {
//		return Math.pow(sin(a), 2);
//	}
	private static double cos2(double a) {
		return Math.pow(cos(a), 2);
	}
	public static Vector3d round(Vector3d p) {
		if (Double.isNaN(p.getX())||Double.isInfinite(p.getX())
				||Double.isNaN(p.getY())||Double.isInfinite(p.getY())
				||Double.isNaN(p.getZ())||Double.isInfinite(p.getZ())) return p;
		return new Vector3d(Math.round(1000*p.getX())/1000d, Math.round(1000*p.getY())/1000d, Math.round(1000*p.getZ())/1000d);
	}

	public void transform(Point3d p) {
		m.transform(p);
		p.add(o);
	}

}
