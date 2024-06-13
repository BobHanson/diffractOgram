/* DiffractOgram - OrientationClass.java
 * 
 * Author   : Nicolas Schoeni
 * Creation : 29 nov. 06
 * 
 * nicolas.schoeni@epfl.ch
 */
package org.epfl.diffractogram.transformations;

import java.util.Vector;

import org.epfl.diffractogram.util.Java3dUtil.Transform3D;
import org.epfl.diffractogram.util.Java3dUtil.TransformGroup;
import org.epfl.diffractogram.util.Java3dUtil.Point3d;
import org.epfl.diffractogram.util.Java3dUtil.Vector3d;

public class OrientationClass {
	public double omega, chi, phi;
	private Vector<OrientationObject> v;
	private Transform3D t3d1, t3d2, t3d3, t3d23, t3di;
	public Transform3D t3d;
	public boolean mathOnly = false;
	
	public class OrientationObject extends TransformGroup {
		public TransformGroup tgOmegaOnly;
		
		public OrientationObject() {
			setCapabilityTo(TransformGroup.ALLOW_TRANSFORM_WRITE);
			tgOmegaOnly = new TransformGroup();
			tgOmegaOnly.setCapabilityTo(TransformGroup.ALLOW_TRANSFORM_WRITE);
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
