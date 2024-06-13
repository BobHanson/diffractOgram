/* DiffractOgram - PrecessionClass.java
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

public class PrecessionClass {
	protected Transform3D t3dx, t3dz, t3dy, t3dReverse, t3dRot;
	public Transform3D t3d;
	public double mu, alpha;
	public double angleX, angleZ;
	private Vector<PrecessionObject> v;
	public boolean mathOnly = false;

	public PrecessionClass() {
		t3dx = new Transform3D();
		t3dz = new Transform3D();
		t3d = new Transform3D();
		t3dReverse = new Transform3D();
		t3dy = new Transform3D();
		t3dRot = new Transform3D();
		v = new Vector<PrecessionObject>(10, 10);
	}
	
	public class PrecessionObject extends TransformGroup {
		public PrecessionObject() {
			setCapabilityTo(TransformGroup.ALLOW_TRANSFORM_WRITE);
			v.add(this);
		}
		
		public void update() {
			super.setTransform(t3d);
		}
	}
	
	public class PrecessionRotObject extends PrecessionObject {
		public void update() {
			super.setTransform(t3dRot);
		}
	}
	
	public void setAngle(double mu) {
		this.mu=Math.PI*mu/180;
		calculateTransform();
		if (!mathOnly)
			for (int i=0; i<v.size(); i++) {
				((PrecessionObject)v.get(i)).update();
			}
	}
	public void setRotation(double alpha) {
		this.alpha=Math.PI*alpha/180;
		calculateTransform();
		if (!mathOnly)
			for (int i=0; i<v.size(); i++)
				((PrecessionObject)v.get(i)).update();
	}
	
	private void calculateTransform() {
		angleX = mu*Math.cos(alpha);
		angleZ = mu*Math.sin(alpha);
		t3dx.rotX(angleX);
		t3dz.rotZ(angleZ);
		t3dy.rotY(-alpha);
		t3d.mul(t3dz, t3dx);
		t3dx.rotX(-angleX);
		t3dz.rotZ(-angleZ);
		t3dReverse.mul(t3dx, t3dz);
		t3dRot.mul(t3d, t3dy);
	}
	
	public void apply(Point3d p) {
		if (alpha!=0 || mu!=0) {
			t3d.transform(p);
		}
	}
	public void apply(Vector3d v) {
		if (alpha!=0 || mu!=0) {
			t3d.transform(v);
		}
	}
	public void reverse(Point3d p) {
		if (alpha!=0 || mu!=0) {
			t3dReverse.transform(p);
		}
	}
	public void reverse(Vector3d v) {
		if (alpha!=0 || mu!=0) {
			t3dReverse.transform(v);
		}
	}
	public void applyRot(Point3d p) {
		if (alpha!=0 || mu!=0) {
			t3dRot.transform(p);
		}
	}
	public void applyRot(Vector3d v) {
		if (alpha!=0 || mu!=0) {
			t3dRot.transform(v);
		}
	}
}
