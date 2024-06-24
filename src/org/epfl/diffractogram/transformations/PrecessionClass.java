/* DiffractOgram - PrecessionClass.java
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
 * A class to maintain an overall Transform3D t3d and the precession angle mu
 * and rotation alpha along with a vector of PrecessionClass.PrecessionObject
 * and its subclass PrecessionRotObject.
 * 
 * <pre>
 * 
 * Precessionobject objects include:
 *      
 *      model3d.projScreen3d.rotTg
 *      model3d.net.precessionObject
 *      model3d.mask3d.pTg
 *
 * PrecessionRotObject objects include:
 *
 *      model3d.mask3d.rotTg
 *
 * </pre>
 */
public class PrecessionClass {

	public Transform3D t3d;
	public double mu, alpha;

	private Transform3D t3dx, t3dz, t3dy, t3dReverse, t3dRot;
	private double angleX, angleZ;
	private Vector<PrecessionObject> v;
	private final static boolean mathOnly = false;

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
			setName("precessionobject");
			setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			v.add(this);
		}

		public void update() {
			super.setTransform(t3d);
		}
	}

	public class PrecessionRotObject extends PrecessionObject {
		public PrecessionRotObject() {
			setName("precessionrotobject");
		}

		public void update() {
			super.setTransform(t3dRot);
		}
	}

	public void setAngle(double mu) {
		this.mu = Math.PI * mu / 180;
		calculateTransform();
		if (!mathOnly)
			for (int i = 0; i < v.size(); i++) {
				((PrecessionObject) v.get(i)).update();
			}
	}

	public void setRotation(double alpha) {
		this.alpha = Math.PI * alpha / 180;
		calculateTransform();
		if (!mathOnly)
			for (int i = 0; i < v.size(); i++)
				((PrecessionObject) v.get(i)).update();
	}

	private void calculateTransform() {
		angleX = mu * Math.cos(alpha);
		angleZ = mu * Math.sin(alpha);
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
		if (alpha != 0 || mu != 0) {
			t3d.transform(p);
		}
	}

	public void apply(Vector3d v) {
		if (alpha != 0 || mu != 0) {
			t3d.transform(v);
		}
	}

	public void reverse(Point3d p) {
		if (alpha != 0 || mu != 0) {
			t3dReverse.transform(p);
		}
	}

	public void reverse(Vector3d v) {
		if (alpha != 0 || mu != 0) {
			t3dReverse.transform(v);
		}
	}

	public void applyRot(Point3d p) {
		if (alpha != 0 || mu != 0) {
			t3dRot.transform(p);
		}
	}

	public void applyRot(Vector3d v) {
		if (alpha != 0 || mu != 0) {
			t3dRot.transform(v);
		}
	}
}
