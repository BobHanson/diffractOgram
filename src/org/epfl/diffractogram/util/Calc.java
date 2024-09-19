package org.epfl.diffractogram.util;

import java.awt.Point;
import java.awt.geom.Point2D.Double;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Calc {

	public static synchronized float calcIntensity(Vector3d a, Vector3d b, Vector3d c, int h, int k, int l) {
		Vector3d v = new Vector3d();
		v.scaleAdd(h, Utils3d.norm(a), v);
		v.scaleAdd(k, Utils3d.norm(b), v);
		v.scaleAdd(l, Utils3d.norm(c), v);
		double r = v.length();
		double i = 8 * Math.exp(-r/2) + .2;
		return (float) Math.min(Math.max(i, 0.4), 1);
	}

	public static boolean projPointFlat(Point3d p, Vector3d n, double d) {
		double t = d / Utils3d.dot(n, p);
		if (t < 0)
			return false;
		p.scale(t);
		return true;
	}

	public static Double projTo2dFlat(Vector3d p, double w, double h) {
		if (p.x > w / 2 || -p.x > w / 2 || p.y > h / 2 || -p.y > h / 2)
			return null;
		return new Point.Double(p.x / w, -p.z / h);
	}

	public static boolean projPointCylinder(Point3d v, Vector3d n, double d, double y) {
		double t = Math.sqrt((y * y) / ((v.x * v.x) + (v.y * v.y)));
		if (t < 0)
			return false;
		v.scale(t);
		return true;
	}

	public static Double projTo2dCylinder(Vector3d p, double h) {
		if (p.z < -h / 4 || p.z > h / 4)
			return null;
		// vOriented.scale(t);
//		double d = Math.atan(p.y/p.x)/Math.PI;
//		return new Point.Double(-d, -p.z/h*2);
		double d = Math.atan(p.x / p.y) / Math.PI / 2.0;
		return new Point.Double(d + (p.y < 0 ? (p.x < 0 ? -.5 : .5) : 0), -p.z / h * 2);
	}


}