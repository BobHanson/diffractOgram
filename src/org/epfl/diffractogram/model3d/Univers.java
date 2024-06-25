package org.epfl.diffractogram.model3d;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;

import org.epfl.diffractogram.util.WorldRenderer;

/**
 * The Univers class maintains the Canvas3D element and the universe. Fields include:
 * 
 * canvas3D   a Canvas3D object based on a SimpleUniverse's preferred configuration.
 * 
 * root the root BranchGroup, with attached child inner class Univers.UniversBehavior and Model3d shapes
 * 
 * tg the TransformGroup, parent of root
 * 
 * reset a TransformGroup, parent of tg
 * 
 * environment a BranchGroup, parent of reset, BoundingLeaf, Background, and two DirectionalLight objects 
 * 
 * u  the SimpleUniverse for this canvas, parent to BranchGroups root and environment.
 * 
 * Thus:
 * 
 * <pre>
 *      u  SimpleUniverse
 *       \
 *        environment  BranchGroup
 *         \  \  \  \    
 *          \  \  \  light1, light2  Directional Lights
 *           \  \  background  Background
 *            \  boundingLeaf(bounds) BoundingLeaf
 *             \
 *              reset TransformGroup
 *               \
 *                tg TransformGroup
 *                 \
 *                  root BranchGroup
 *                   \  \
 *                    \  UniversBehavior (mouse event processing)
 *                     \
 *                      Model3d shapes          
 *
 *</pre>  
 */
public class Univers {
	private WorldRenderer renderer;
	private TransformGroup tgReset;
	private TransformGroup tgTop;
	private BranchGroup root;
	
	public Univers(JPanel panel3d) {

		renderer = WorldRenderer.createWorldRenderer(panel3d, this);
		
		// tgReset is the highest transform group. 
		// It will set the starting default view
		tgReset = new TransformGroup();
		tgReset.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		// tgTop transform group will receive the mouse actions
		// it is the parent of root and is started with rotx -90, roty -90
		tgTop = new TransformGroup();
		tgTop.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		tgTop.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		// root is the root for all objects in the scene
		// it is 
		root = renderer.getRootBranchGroup();
		root.setName("root");
		root.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		root.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		root.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

		Utils3d.setParents(root, tgTop, tgReset);
		renderer.setEnvironment(tgReset);
	}
	
	public TransformGroup newTransformGroup(Transform3D t3d) {
		return renderer.newTransformGroup(t3d);
	}

	public boolean isParallel() {
		return renderer.isParallel();
	}
	
	public void setParallel(boolean b) {
		renderer.setParallel(b);
	}

	public void applyTransform(Transform3D t3d) {
		Transform3D cur = new Transform3D();
		tgTop.getTransform(cur);
		cur.mul(t3d, cur);
		tgTop.setTransform(cur);
	}

	public void rotX(double angle) {
		Transform3D t3d = new Transform3D();
		t3d.rotX(angle*Math.PI/180);
		applyTransform(t3d);
	}
	public void rotY(double angle) {
		Transform3D t3d = new Transform3D();
		t3d.rotY(angle*Math.PI/180);
		applyTransform(t3d);
	}
	public void rotZ(double angle) {
		Transform3D t3d = new Transform3D();
		t3d.rotZ(angle*Math.PI/180);
		applyTransform(t3d);
	}
	
	public void scale(double s) {
		Transform3D t3d = new Transform3D();
		t3d.set(s);
		applyTransform(t3d);
	}
	
	/**
	 * This is only called once.
	 * 
	 */
	public void reset() {
		renderer.reset(tgReset);
	}
	
	public void setBackgroundColor(Color3f bgColor) {
		renderer.setBackgroundColor(bgColor);
	}
	
	public void cleanup() {
		renderer.cleanup();
	}
	
	public interface Selectable {
		public void click();
	}

	public BranchGroup getRoot() {
		return root;
	}

	public void removeNotify(Group parent, Node child) {
		if (parent == null)
			parent = root;
		renderer.notifyRemove(parent, child);
		parent.removeChild(child);
	}

	public void addNotify(Group parent, Node child) {
		if (parent == null)
			parent = root;
		parent.addChild(child);
		renderer.notifyAdd(parent, child);
	}

	public void removeAllNotify(Group g) {
		renderer.notifyRemoveAll(g);
		g.removeAllChildren();
	}

	public void complete() {
		renderer.complete();
	}

	public void setTopTransform() {
		renderer.setTopTransform(tgTop);
	}

}


