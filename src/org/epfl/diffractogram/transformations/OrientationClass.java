/* DiffractOgram - OrientationClass.java
 * 
 * Author   : Nicolas Schoeni
 * Creation : 29 nov. 06
 * 
 * nicolas.schoeni@epfl.ch
 */
package org.epfl.diffractogram.transformations;

import java.util.Vector;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * with TransformGroup subclass OrientationClass.OrientationObject.
 * 
 * 
 * The singleton OrientationClass object model3d.orientationClass maintains the
 * sample orientation angles omega, chi, and phi as well as an overall
 * Transform3D t3d.
 * 
 * All OrientationObjects added to this class are transformed by t3d, 
 * but the goniometer ring orientation is only set by omega, so it is 
 * made to be an omega-only object, the sole child of tgOmegaOnly.
 * 
 * <pre>
 * 
 * OrientationObjects include:
 * 
 * model3d.net.orientationObject 
 * model3d.net.gonioHead.orientationGonio
 * 
 * </pre>
 */
public class OrientationClass {
	
	public Transform3D t3d;
	public double omega, chi, phi;
	
	private Vector<OrientationObject> v;
	private Transform3D t3d1, t3d2, t3d3, t3d23, t3di;
	private final static boolean mathOnly = false;
	
	/**
	 * 
	 *
	 */
	public class OrientationObject extends TransformGroup {
		public TransformGroup tgOmegaOnly;
		
		public OrientationObject() {
			setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			tgOmegaOnly = new TransformGroup();
			tgOmegaOnly.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			v.add(this);
		}
	}
	
	public OrientationClass() {
		t3d1 = new Transform3D();
		t3d2 = new Transform3D();
		t3d3 = new Transform3D();
		t3d23 = new Transform3D();
		t3d = new Transform3D();
		t3di = new Transform3D();
		v = new Vector<OrientationObject>(10, 10);
	}

	public void setOmega(double angle) {
		omega=Math.PI*angle/180;
		t3d1.rotZ(omega);
		t3d23.mul(t3d1, t3d2);
		t3d.mul(t3d23, t3d3);
		t3di.invert(t3d);
		if (!mathOnly)
			for (int i=0; i<v.size(); i++) {
				OrientationObject o = (OrientationObject)v.get(i);
				o.setTransform(t3d);
				o.tgOmegaOnly.setTransform(t3d1);
			}
	}
	public void setChi(double angle) {
		chi=Math.PI*angle/180;
		t3d2.rotY(chi);
		t3d23.mul(t3d1, t3d2);
		t3d.mul(t3d23, t3d3);
		t3di.invert(t3d);
		if (!mathOnly)
			for (int i=0; i<v.size(); i++)
				((OrientationObject)v.get(i)).setTransform(t3d);
	}
	public void setPhi(double angle) {
		phi=Math.PI*angle/180;
		t3d3.rotZ(phi);
		t3d.mul(t3d23, t3d3);
		t3di.invert(t3d);
		if (!mathOnly)
			for (int i=0; i<v.size(); i++)
				((OrientationObject)v.get(i)).setTransform(t3d);
	}
	
	public void apply(Point3d p) {
		t3d.transform(p);
	}
	public void apply(Vector3d v) {
		t3d.transform(v);
	}
	public void reverse(Point3d p) {
		t3di.transform(p);
	}
	public void reverse(Vector3d v) {
		t3di.transform(v);
	}

	public void applyOmega(Vector3d v) {
		t3d1.transform(v);
	}
	public void applyChi(Vector3d v) {
		t3d2.transform(v);
	}
	public void applyPhi(Vector3d v) {
		t3d3.transform(v);
	}
}
