package org.jmol.j3d;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.epfl.diffractogram.model3d.Univers;
import org.epfl.diffractogram.model3d.Utils3d;
import org.epfl.diffractogram.util.WorldRenderer;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.j3d.geometry.JmolArrow;
import org.jmol.j3d.geometry.JmolBox;
import org.jmol.j3d.geometry.JmolCylinder;
import org.jmol.j3d.geometry.JmolQuad;
import org.jmol.j3d.geometry.JmolShape3D;
import org.jmol.j3d.geometry.JmolSphere;
import org.jmol.j3d.geometry.JmolText;
import org.jmol.j3d.geometry.JmolTorus;

/**
 * A class to leverage Jmol's g3d engine and perspective model. 
 * Shapes are limited -- just arrow, box, cylinder, quad, sphere, text, and torus. 
 * 
 * Text and Torus will be interesting challenges.  
 * 
 * 
 * 
 * @author Bob Hanson
 */
public class JmolWorldRenderer extends WorldRenderer {

	@SuppressWarnings("serial")
	class JmolPanel extends JPanel {

		private final Dimension currentSize = new Dimension();

		JmolPanel() {
			viewer = JmolViewer.allocateViewer(this, new SmarterJmolAdapter(), null, null, null, null, null);
		}

		@Override
		public void paint(Graphics g) {
			getSize(currentSize);
			viewer.renderScreenImage(g, currentSize.width, currentSize.height);
		}
	}

	JmolViewer viewer;

	public JmolWorldRenderer(JPanel panel3d, Univers univers) {
		super(panel3d, univers);
		JmolPanel jmol = new JmolPanel();
		panel3d.add(jmol);
	}

	@Override
	public void setEnvironment(TransformGroup reset) {
	}

	@Override
	public boolean isParallel() {
		return true;
	}

	@Override
	public void setParallel(boolean b) {
		// n/a
	}

	@Override
	public void setBackgroundColor(Color3f bgColor) {
		viewer.setColorBackground("["+ bgColor.x +","+ bgColor.y + "," +  bgColor.z + "]");
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	public static List<Node> allObjects = new ArrayList<>();

	public static BranchGroup getTextShapeImpl(String name, BranchGroup bg, String s, Point3d pos, float size,
			Font font, int align, int path, Point3d rotPoint, Appearance app) {
		Shape3D textShape = new JmolText(name, bg, s, font, align, path);

		Transform3D tsize = new Transform3D();
		tsize.set(size, new Vector3d(pos));

		Utils3d.setParents(textShape, new TransformGroup(tsize), bg);

		return bg;
	}

	public static Node createTorusImpl(String name, double innerRadius, double outerRadius, int innerFaces,
			int outerFaces, Appearance app) {
		return new JmolTorus(name, innerRadius, outerRadius, innerFaces, outerFaces, app);
	}

	public static Node createBoxImpl(String name, double dx, double dy, double dz, Appearance app) {
		return new JmolBox(name, dx, dy, dz, app);
	}

	public static Node createCylinderImpl(String name, double radius, double height, boolean isHollow, int xdiv,
			int ydiv, Appearance app) {
		return new JmolCylinder(name, radius, height, isHollow, xdiv, ydiv, app);
	}

	public static Node createSphereImpl(String name, double radius, int divs, boolean isAtom, Appearance app) {
		return new JmolSphere(name, radius, divs, isAtom, app);
	}

	public static TransformGroup createArrowImpl(String name, TransformGroup tg, double radiusArrow, double lenArrow,
			double radius, float height, int precision, Appearance app) {
		tg.addChild(new JmolArrow(name, radiusArrow, lenArrow, radius, height, precision, app));
		return tg;
	}

	public static Node createQuadImpl(String name, QuadArray quad, Appearance app) {
		return new JmolQuad(name, quad, app);
	}

	@Override
	public void reset(TransformGroup reset) {

	}

	@Override
	public void notifyRemove(Group parent, Node child) {
		System.out.println("removed " + child.getName() + " from " + parent.getName());
		switch(parent.getName()) {
		case "root":
			this.mapRoot.remove(child.getName());
			break;
		}
	}

	@Override
	public void notifyAdd(Group parent, Node child) {
		switch(parent.getName()) {
		case "root":
			System.out.println("added " + child.getName() + " to " + parent.getName());
			this.mapRoot.put(child.getName(), child);
			break;
		}
	}

	@Override
	public void notifyRemoveAll(Group g) {
		// TODO Auto-generated method stub

	}

	@Override
	public TransformGroup newTransformGroup(Transform3D t3d) {
		return (t3d == null ? new JmolTransformGroup() : new JmolTransformGroup(t3d));
	}

	public static class JmolTransformGroup extends TransformGroup {

		public JmolTransformGroup() {
			super();
		}

		public JmolTransformGroup(Transform3D t3d) {
			super(t3d);		}

		private Transform3D tlast;

		public void setTransform(Transform3D t3d) {
			if (!t3d.equals(tlast))
				super.setTransform(t3d);
			if (tlast == null) {
				tlast = new Transform3D();
			} else {
				Shape3D n = null;
				try {
					n = Utils3d.getShapeChild(this);
//					System.out.println("TG " + (n != null ? n.getName() : getName()) + " " + t3d);
				} catch (Exception e) {

				}
			}
			tlast.set(t3d);
		}
	}

	public void complete() {
		String s = "background white;";
		for (int i = 0, n = allObjects.size(); i < n; i++) {
			Node node = allObjects.get(i);
			if (node instanceof JmolShape3D) {
				s += ((JmolShape3D) node).renderScript(this);
			}
		}
		viewer.script(s);

		
		
	}

}
