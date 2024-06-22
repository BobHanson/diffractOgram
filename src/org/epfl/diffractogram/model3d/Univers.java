package org.epfl.diffractogram.model3d;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

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
	private TransformGroup tg, reset;
	public BranchGroup root;
	
	public Univers(JPanel panel3d) {

		renderer = WorldRenderer.createWorldRenderer(panel3d, this);
		// this is the root for all objects in the scene
		root = new BranchGroup();
		root.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		root.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		root.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

		// this transform group will receive the mouse actions
		tg = new TransformGroup();
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg.addChild(root);

		// this transform group will receive the reset transform
		reset = new TransformGroup();
		reset.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		reset.addChild(tg);

		// reset the transform group
		reset();

		renderer.setEnvironment(reset, root);
	}
	
	public boolean isParallel() {
		return renderer.isParallel();
	}
	
	public void setParallel(boolean b) {
		renderer.setParallel(b);
	}

	public void applyTransform(Transform3D t3d) {
		Transform3D cur = new Transform3D();
		tg.getTransform(cur);
		cur.mul(t3d, cur);
		tg.setTransform(cur);
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
	
	public void reset() {
		Transform3D t3d = new Transform3D();
		t3d.set(new Vector3d(0, 0, -5), .2);
		reset.setTransform(t3d);
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
	
}


