/* DiffractOgram - Mask3d.java
 * 
 * Author   : Nicolas Schoeni
 * Creation : 29 juin 2005
 * 
 * nicolas.schoeni@epfl.ch
 */
package org.epfl.diffractogram.model3d;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.epfl.diffractogram.diffrac.DefaultValues;
import org.epfl.diffractogram.transformations.PrecessionClass;
import org.epfl.diffractogram.util.WorldRenderer;

public class Mask3d extends BranchGroup {
	private TransformGroup rotTg, transTg, resizeTg, torTransTg, pTg;
	private Transform3D t3d;
	private double y, r;
	private Point3d center = new Point3d();
	private DefaultValues defaultValues;
	private PrecessionClass precessionClass;

	public Mask3d(Univers univers, PrecessionClass precessionClass, DefaultValues defaultValues, double y, double r, double w,
			double h) {
		setName("mask3d");
		this.precessionClass = precessionClass;
		this.defaultValues = defaultValues;
		setCapability(BranchGroup.ALLOW_DETACH);

		t3d = new Transform3D();

		rotTg = precessionClass.new PrecessionRotObject();
		pTg = precessionClass.new PrecessionObject();
		transTg = univers.newTransformGroup(null);
		resizeTg = univers.newTransformGroup(null);

		torTransTg = univers.newTransformGroup(null);
		transTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		resizeTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		rotTg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		rotTg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		torTransTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		Utils3d.setParents(WorldRenderer.createCylinder("maskcyl", 0.3, 0.1, false, 50, 5,
				Utils3d.createApp(new Color3f(.8f, .8f, .8f), .5f)), 
				resizeTg, pTg, transTg, this);

		Utils3d.setParents(WorldRenderer.createTorus("maskframe",.04, 1, 10, 50, Utils3d.createApp(ColorConstants.black)), 
				torTransTg, rotTg, transTg, this);

		BranchGroup label = Utils3d.createFixedLegend("Precession mask", new Point3d(), .2f,
				Utils3d.createApp(ColorConstants.black), false);
		Utils3d.setParents(label, Utils3d.getVectorTransformGroup(-w / 7, 0, h / 3.2, t3d), pTg);
		setR(r);
		setY(y);
		setWH(w, h);
	}

	public Point3d center() {
		center.set(0, 0, r / defaultValues.maskDistFract);
		precessionClass.applyRot(center);
		center.y += y;
		return center;
	}

	public void setR(double r) {
		this.r = r;
		t3d.set(r, new Vector3d(0, 0, r));
		torTransTg.setTransform(t3d);
	}

	public void setY(double y) {
		this.y = y;
		t3d.set(new Vector3d(0, y * defaultValues.maskDistFract, 0));
		transTg.setTransform(t3d);
	}

	public void setWH(double w, double h) {
		Matrix3d m = new Matrix3d(w, 0, 0, 0, 1, 0, 0, 0, h);
		t3d.set(m);
		resizeTg.setTransform(t3d);
	}
}
