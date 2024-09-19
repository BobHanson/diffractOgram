/* DiffractOgram - Rays.java
 * 
 * Author   : Nicolas Schoeni
 * Creation : 1 juil. 2005
 * 
 * nicolas.schoeni@epfl.ch
 */
package org.epfl.diffractogram.model3d;

import java.util.Vector;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point3d;

import org.epfl.diffractogram.util.Colors;
import org.epfl.diffractogram.util.Utils3d;


public class Rays extends BranchGroup {
	
	private Vector<BranchGroup> raysAnt, raysUsed;
	private final Appearance raysAppTransp;
	private BranchGroup impacts;
	private Model3d model3d;
	public static final Point3d o = new Point3d(0, 0, 0);
	
	public Rays(Model3d model3d) {
		this.model3d = model3d;
		setName("rays");
		raysAnt = new Vector<BranchGroup>(100, 100);
		raysUsed = new Vector<BranchGroup>(100, 100);
		setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		raysAppTransp = Utils3d.newAppearance("ray:transp");
		raysAppTransp.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.FASTEST,1f));

		impacts = new BranchGroup();
		impacts.setName("impact:");
		impacts.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		impacts.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		model3d.univers.addNotify(this, impacts);
	}
	
	
	public void removeAllRays(boolean persistent) {
		BranchGroup r;
		if (!persistent)
			model3d.univers.removeAllNotify(impacts);
		for (int i=raysUsed.size()-1; i>=0; i--) {
			r = (BranchGroup)raysUsed.get(i);
			Utils3d.changeCylinderApp(r, raysAppTransp);
			raysUsed.remove(r);
			raysAnt.add(r);
		}
	}
	
	private static int rayid;
	
 BranchGroup createRay(Point3d a, Point3d b, Appearance app) {
		BranchGroup r;
		if (raysAnt.size() == 0) {
			r = model3d.univers.creator.createCylinder(model3d.univers, "ray:" + ++rayid, a, b, .02, app, 4);
			model3d.univers.addNotify(this, r);
		} else {
			r = (BranchGroup) raysAnt.remove(raysAnt.size() - 1);
			Utils3d.changeCylinder(r, a, b);
			Utils3d.changeCylinderApp(r, app);
		}
		raysUsed.add(r);
		return r;
	}
	
	static int impactid;
	
	public void addImpactRay(Point3d cSphere, Point3d pNet, Point3d pOrigin, Point3d pProj) {
		if (pNet != null)
			createRay(cSphere, pNet, Colors.appRed);
		createRay(pOrigin, pProj, Colors.appRed);
		model3d.univers.addNotify(impacts, model3d.univers.creator.createAtom("impact:" + ++impactid, pProj, Colors.black, .03f));
	}

	

}
