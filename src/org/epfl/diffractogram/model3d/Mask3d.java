/* DiffractOgram - Mask3d.java
 * 
 * Author   : Nicolas Schoeni
 * Creation : 29 juin 2005
 * 
 * nicolas.schoeni@epfl.ch
 */
package org.epfl.diffractogram.model3d;

import org.epfl.diffractogram.diffrac.DefaultValues;
import org.epfl.diffractogram.transformations.PrecessionClass;
import org.epfl.diffractogram.util.Java3dUtil.BranchGroup;
import org.epfl.diffractogram.util.Java3dUtil.Color3f;
import org.epfl.diffractogram.util.Java3dUtil.Cylinder;
import org.epfl.diffractogram.util.Java3dUtil.Matrix3d;
import org.epfl.diffractogram.util.Java3dUtil.Point3d;
import org.epfl.diffractogram.util.Java3dUtil.Transform3D;
import org.epfl.diffractogram.util.Java3dUtil.TransformGroup;
import org.epfl.diffractogram.util.Java3dUtil.Vector3d;
import org.epfl.diffractogram.util.Java3dUtil.Torus;


public class Mask3d extends BranchGroup {
	private TransformGroup rotTg, transTg, resizeTg, torTransTg, pTg;
	private Transform3D t3d;
	private double y, r;
	private Point3d center = new Point3d(); 
	private DefaultValues defaultValues;
	private PrecessionClass precessionClass;

	public Mask3d(PrecessionClass precessionClass, DefaultValues defaultValues, double y, double r, double w,
			double h) {
		this.precessionClass = precessionClass;
		this.defaultValues = defaultValues;
		setCapability(BranchGroup.ALLOW_DETACH);

		t3d = new Transform3D();
		rotTg = precessionClass.new PrecessionRotObject();
		pTg = precessionClass.new PrecessionObject();
		transTg = new TransformGroup();
		resizeTg = new TransformGroup();
		torTransTg = new TransformGroup();
		transTg.setCapabilityTo(TransformGroup.ALLOW_TRANSFORM_WRITE);
		resizeTg.setCapabilityTo(TransformGroup.ALLOW_TRANSFORM_WRITE);
		rotTg.setCapabilityTo(BranchGroup.ALLOW_CHILDREN_EXTEND);
		rotTg.setCapabilityTo(BranchGroup.ALLOW_CHILDREN_WRITE);
		torTransTg.setCapabilityTo(TransformGroup.ALLOW_TRANSFORM_WRITE);

		pTg.addChild(resizeTg);
		transTg.addChild(rotTg);
		transTg.addChild(pTg);
		addChild(transTg);

		Torus tor = new Torus(.04f, (float) 1, 10, 50, Utils3d.createApp(ColorConstants.black));
		torTransTg.addChild(tor);
		rotTg.addChild(torTransTg);

		Cylinder b = new Cylinder(.3f, .1f, Cylinder.GENERATE_NORMALS, 50, 5,
				Utils3d.createApp(new Color3f(.8f, .8f, .8f), .5f));
		resizeTg.addChild(b);

		BranchGroup label = Utils3d.createFixedLegend("Precession mask", new Point3d(), .2f,
				Utils3d.createApp(ColorConstants.black), false);
		t3d.set(new Vector3d(-w / 7, 0, h / 3.2));
		TransformGroup labelTransTg = new TransformGroup(t3d);
		labelTransTg.addChild(label);
		pTg.addChild(labelTransTg);

		setR(r);
		setY(y);
		setWH(w, h);
	}
	
	public Point3d center() {
		center.set(0, 0, r/defaultValues.maskDistFract);
		precessionClass.applyRot(center);
		center.y+=y;
		return center;
	}
	
	public void setR(double r) {
		this.r=r;
		t3d.set(r, new Vector3d(0, 0, r));
		torTransTg.setTransform(t3d);
	}
	public void setY(double y) {
		this.y=y;
		t3d.set(new Vector3d(0, y*defaultValues.maskDistFract, 0));
		transTg.setTransform(t3d);
	}
	public void setWH(double w, double h) {
		Matrix3d m = new Matrix3d(w, 0, 0, 0, 1, 0, 0, 0, h); 
		t3d.set(m);
		resizeTg.setTransform(t3d);
	}
}
