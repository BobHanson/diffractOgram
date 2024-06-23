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


public class Rays extends BranchGroup {
	private Vector<BranchGroup> raysAnt, raysUsed;
	private final Appearance raysAppRed, raysAppWhite, raysAppTransp;
	private BranchGroup impacts;
	public static final Point3d o = new Point3d(0, 0, 0);
	
	public Rays() {
		raysAnt = new Vector<BranchGroup>(100, 100);
		raysUsed = new Vector<BranchGroup>(100, 100);
		setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		raysAppRed = Utils3d.createApp(ColorConstants.red);
		raysAppWhite = Utils3d.createApp(ColorConstants.white);
		raysAppTransp = Utils3d.newAppearance("ray:transp");
		raysAppTransp.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.FASTEST,1f));

		impacts = new BranchGroup();
		impacts.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		impacts.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		addChild(impacts);
	}
	
	
	public void removeAllRays() {
		BranchGroup r;
		impacts.removeAllChildren();
		for (int i=raysUsed.size()-1; i>=0; i--) {
			r = (BranchGroup)raysUsed.get(i);
			Utils3d.changeCylinderApp(r, raysAppTransp);
			raysUsed.remove(r);
			raysAnt.add(r);
		}
	}
	
	private BranchGroup ray(Point3d a, Point3d b, Appearance app) {
		BranchGroup r;
		if (raysAnt.size()==0) {
			r = Utils3d.createCylinder(a, b, .02, app, 4);
			addChild(r);
		}
		else {
			r = (BranchGroup) raysAnt.remove(raysAnt.size()-1);
			Utils3d.changeCylinder(r, a, b);
			Utils3d.changeCylinderApp(r, app);
		}
		raysUsed.add(r);
		return r;
	}
	
	public void addRay(Point3d cSphere, Point3d pNet, Point3d pProj) {
		ray(cSphere, pNet, raysAppRed);
		ray(o, pProj, raysAppWhite);
		impacts.addChild(Utils3d.atom(pProj, ColorConstants.black, .03f));
	}
	

}
