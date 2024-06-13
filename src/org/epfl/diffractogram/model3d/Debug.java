/* DiffractOgram - Debug.java
 * 
 * Author   : Nicolas Schoeni
 * Creation : 5 juil. 2005
 * 
 * nicolas.schoeni@epfl.ch
 */
package org.epfl.diffractogram.model3d;

public class Debug {
	//public static BranchGroup root, MpMo;

	/*
	static {
		root = new BranchGroup();
		root.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		root.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		Univers.univers.root.addChild(root);

		MpMo = new BranchGroup();
		MpMo.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		MpMo.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		
		BranchGroup bg = new BranchGroup(); 
		Orientation o = new Orientation();
		Precession p = new Precession();
		o.addChild(MpMo);
		p.addChild(o);
		bg.addChild(p);
		Univers.univers.root.addChild(bg);
	}
	public static void vector(BranchGroup bg, Tuple3d v, Tuple3d p, Color3f c, double r) {
		Point3d q = new Point3d(p);
		q.add(v);
		bg.addChild(Utils3d.createCylinder(new Point3d(p), q, r, Utils3d.createApp(c), 8));
	}
	public static void point(BranchGroup bg, Tuple3d p, Color3f c, double r) {
		bg.addChild(Utils3d.atom(new Point3d(p), c, (float)r));
	}
	public static void transparentSphere(BranchGroup bg, Point3d p, Color c, double r) {
		bg.addChild(Utils3d.atom(p, VirtualSphere.app(c), r, 50));
	}
	public static void transparentScreen(BranchGroup bg, Vector3d p, Vector3d e1, Vector3d e2, Vector3d e3, double w, double h, Color3f c) {
		Transform3D t3d = new Transform3D(); 
		Precession rotTg = new Precession();
		t3d.set(p);
		TransformGroup transTg = new TransformGroup(t3d);
		transTg.addChild(rotTg);
		BranchGroup bg2 = new BranchGroup();
		bg2.setCapability(BranchGroup.ALLOW_DETACH);
		bg2.addChild(transTg);
		rotTg.addChild(new Shape3D(ProjScreen3d.Flat.createQuad(e1, e2, e3, w, h), ProjScreen3d.app(c)));
		bg.addChild(bg2);
	}
	public static void clear() {
		root.removeAllChildren();
		MpMo.removeAllChildren();
	}
*/	
}
