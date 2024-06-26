package org.epfl.diffractogram.model3d;

import java.awt.Font;
import java.awt.Label;
import java.awt.event.KeyEvent;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.epfl.diffractogram.util.Utils3d;
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
public abstract class Univers {
	public WorldRenderer renderer;
	private TransformGroup tgReset;
	private TransformGroup tgTop;
	private BranchGroup root;
	public Creator creator;
	public Univers(JPanel panel3d) {

		renderer = getRenderer(panel3d);
		
		creator = new Creator();
		
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
	
	abstract protected WorldRenderer getRenderer(JPanel panel3d);

	public TransformGroup newWritableTransformGroup(Transform3D t3d) {
		TransformGroup g = renderer.newTransformGroup(t3d);
		g.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		return g;
	}

	public boolean isParallel() {
		return renderer.isParallel();
	}
	
	public void setParallel(boolean b) {
		renderer.setParallel(b);
	}

	
	/**
	 * Apply the view transform
	 * @param t3d
	 */
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
	
	/**
	 * never called
	 * 
	 * @param bgColor
	 */
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
	

	public class Creator {

		public BranchGroup createCylinder(Univers univers, String name, Point3d b, Point3d a, double radius, Appearance cylApp, int precision) {
			BranchGroup cylBg = new BranchGroup();
			cylBg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
			cylBg.setCapability(BranchGroup.ALLOW_DETACH);

			Vector3f center = new Vector3f();
			Vector3f unit = new Vector3f();
			float height = Utils3d.calculateHeight(b, a, center, unit);
			TransformGroup tg = univers.newWritableTransformGroup(null);
			tg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
			Utils3d.createMatrix(tg, center, unit);
			Transform3D th = new Transform3D();
			th.set(new Matrix3d(1, 0, 0, 0, height, 0, 0, 0, 1));
			TransformGroup tgh = univers.newWritableTransformGroup(th);
			tgh.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
			Utils3d.setParents(renderer.createCylinder(name + ":cyl", radius, 1, false, precision, 1, cylApp), tgh, tg, cylBg);
			cylBg.setName(name);
			return cylBg;
		}

		/**
		 * from Rays
		 * 
		 * @param p
		 * @param color
		 * @param r
		 * @return
		 */
		public BranchGroup createAtom(Point3d p, Color3f color, double r) {
			return createAtom(p, Utils3d.createApp(color), r, 20);
		}

		private int atomid;
		
		public BranchGroup createAtom(Point3d p, Appearance app, double r, int facets) {
			BranchGroup bg = new BranchGroup();
			bg.setCapability(BranchGroup.ALLOW_DETACH);
			Utils3d.setParents(renderer.createSphere("atom:" + ++atomid, r, facets, true, app), Utils3d.getVectorTransformGroup(p.x, p.y, p.z, null), bg);
			return bg;
		}

		public BranchGroup createArrow(String name, Point3d b, Point3d a, double radius, double radiusArrow, double lenArrow,
				Appearance cylApp, int precision) {
			Vector3f center = new Vector3f();
			Vector3f unit = new Vector3f();
			float height = Utils3d.calculateHeight(b, a, center, unit) - (float) lenArrow;
			Vector3d h = new Vector3d();
			h.sub(a, new Vector3d(center));
			h.normalize();
			h.scale(lenArrow / 2);
			center.sub(center, new Vector3f(h));
			TransformGroup tg = new TransformGroup();
			Utils3d.createMatrix(tg, center, unit);
			BranchGroup cylBg = new BranchGroup();
			cylBg.addChild(renderer.createArrow(name, tg, radiusArrow, lenArrow, radius, height, precision, cylApp));
			return cylBg;
		}

		/**
		 * Create a 3D text object in a BranchGroup
		 * 
		 * @param s
		 * @param pos
		 * @param rot      rotation point if this is to rotate (to remain in the plane
		 *                 of the screen), or null if allowed to rotate
		 * @param size
		 * @param app
		 * @param centered
		 * @return
		 */
		public BranchGroup createLegend(String s, Point3d pos, Point3d rot, float size, Appearance app,
				boolean centered) {
			return renderer.createTextShape("text:" + s, new BranchGroup(), s, pos, size, new Font(null, Font.PLAIN, 2),
					centered ? Label.CENTER : Label.LEFT, KeyEvent.VK_RIGHT, rot, app);
		}

		public BranchGroup createFixedLegend(String s, Point3d pos, float size, Appearance app, boolean centered) {
			return createLegend(s, pos, null, size, app, centered);
		}


		public BranchGroup createRepere(Color3f colorText, Color3f colorArrows, Color3f colorCenter, String[] names,
				float sizeText, float sizeArrows, double deltaText, double deltaArrows, Vector3d x, Vector3d y,
				Vector3d z) {
			Appearance app1 = Utils3d.createApp(colorText);
			Appearance app2 = Utils3d.createApp(colorArrows);
			BranchGroup repere = new BranchGroup();

			if (colorCenter != null)
				repere.addChild(renderer.createSphere("axes:o" + names[0], .05, 10, false, Utils3d.createApp(colorCenter)));

			Point3d o = new Point3d(0, 0, 0);
			repere.addChild(createArrow("axes:" + names[0], o, new Point3d(Utils3d.mul(x, (x.length() + deltaArrows) / x.length())), sizeArrows,
					sizeArrows * 2f, sizeArrows * 6f, app2, 12));
			repere.addChild(createArrow("axes:" + names[1], o, new Point3d(Utils3d.mul(y, (y.length() + deltaArrows) / y.length())), sizeArrows,
					sizeArrows * 2f, sizeArrows * 6f, app2, 12));
			repere.addChild(createArrow("axes:" + names[2], o, new Point3d(Utils3d.mul(z, (z.length() + deltaArrows) / z.length())), sizeArrows,
					sizeArrows * 2f, sizeArrows * 6f, app2, 12));

			repere.addChild(createLegend(names[0], new Point3d(Utils3d.mul(x, (x.length() + deltaText) / x.length())), o, sizeText,
					app1, false));
			repere.addChild(createLegend(names[1], new Point3d(Utils3d.mul(y, (y.length() + deltaText) / y.length())), o, sizeText,
					app1, false));
			repere.addChild(createLegend(names[2], new Point3d(Utils3d.mul(z, (z.length() + deltaText) / z.length())), o, sizeText,
					app1, false));
			return repere;
		}

		public BranchGroup createNamedVector(String name, Point3d p1, Point3d p2, Point3d p3, float size,
				Color3f colorText, Color3f colorArrow) {
			Appearance app1 = Utils3d.createApp(colorText);
			Appearance app2 = Utils3d.createApp(colorArrow);
			BranchGroup group = new BranchGroup();
			group.addChild(createArrow(name, p1, p2, .03 * size, .1 * size, .4 * size, app2, 12));
			group.addChild(createFixedLegend(name, p3, .15f * size, app1, true));
			return group;
		}
	}

}


