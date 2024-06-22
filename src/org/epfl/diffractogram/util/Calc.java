package org.epfl.diffractogram.util;

import java.awt.Point;
import java.awt.geom.Point2D.Double;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.epfl.diffractogram.model3d.Utils3d;

public class Calc {

	public static synchronized float calcIntensity(Vector3d a, Vector3d b, Vector3d c, int h, int k, int l) {
		Vector3d v = new Vector3d();
		v.scaleAdd(h, Utils3d.norm(a), v);
		v.scaleAdd(k, Utils3d.norm(b), v);
		v.scaleAdd(l, Utils3d.norm(c), v);
		float r = (float)v.length();
		//return 1f/(4f*r*r)+.03f;
		//return 1f/(4f*(float)Math.pow(r, 1.4))+.03f;
	
		//float i = 2f*(float)Math.exp(-r)+.03f; 
		//float i = 10f*(float)Math.exp(-.3*r)+.3f;
		float i = 8f*(float)Math.exp(-.5*r)+.2f;
		
		if (i < .4f) i=.4f;
		if (i > 1) i=1;
		
		return i;
	}

	public static boolean projPointFlat(Point3d v, Vector3d n, double d) {
		double t = d/(v.getX()*n.getX()+v.getY()*n.getY()+v.getZ()*n.getZ());
		if (t<0) return false;
		v.scale(t);
		return true;
	}

	public static Double projTo2dFlat(Vector3d p, double w, double h) {
		if (p.getX()>w/2||-p.getX()>w/2
				||p.getY()>h/2||-p.getY()>h/2) return null;
		return new Point.Double(p.x/w, -p.getZ()/h);
	}

	public static boolean projPointCylinder(Point3d v, Vector3d n, double d, double y) {
		//vOriented.set(v);
		//Orientation.apply(vOriented);
		//t = Math.sqrt((y*y)/((vOriented.x*vOriented.x)+(vOriented.y*vOriented.y)));
		double t = Math.sqrt((y*y)/((v.x*v.x)+(v.y*v.y)));
		if (t<0) return false;
		v.scale(t);
		return true;
	}

	public static Double projTo2dCylinder(Vector3d p, double h) {
		if (p.getZ()<-h/4 || p.getZ()>h/4) return null;
		//vOriented.scale(t);
//		double d = Math.atan(p.y/p.x)/Math.PI;
//		return new Point.Double(-d, -p.z/h*2);
		double d = Math.atan(p.getX()/p.getY())/Math.PI/2.0;
		return new Point.Double(d+(p.getY()<0?(p.getX()<0?-.5:.5):0), -p.getZ()/h*2);
	}
	
	
	
	
}