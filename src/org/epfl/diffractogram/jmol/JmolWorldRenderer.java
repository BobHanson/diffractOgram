package org.epfl.diffractogram.jmol;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Enumeration;
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
import javax.swing.SwingUtilities;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.epfl.diffractogram.model3d.Univers;
import org.epfl.diffractogram.model3d.WorldRenderer;
import org.epfl.diffractogram.util.Utils3d;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.j3d.WorldRendererI;
import org.jmol.j3d.geometry.JmolArrow;
import org.jmol.j3d.geometry.JmolBox;
import org.jmol.j3d.geometry.JmolCylinder;
import org.jmol.j3d.geometry.JmolQuad;
import org.jmol.j3d.geometry.JmolShape3D;
import org.jmol.j3d.geometry.JmolSphere;
import org.jmol.j3d.geometry.JmolText;
import org.jmol.j3d.geometry.JmolTorus;
import org.jmol.viewer.Viewer;

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
public class JmolWorldRenderer extends WorldRenderer implements WorldRendererI {

	public Viewer viewer;

	@SuppressWarnings("serial")
	class JmolPanel extends JPanel {

		private final Dimension currentSize = new Dimension();

		JmolPanel() {
			viewer = (Viewer) JmolViewer.allocateViewer(this, new SmarterJmolAdapter(), null, null, null, null, null);
		}

		@Override
		public void paint(Graphics g) {
			getSize(currentSize);
			viewer.renderScreenImage(g, currentSize.width, currentSize.height);
		}
	}


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

	public List<Node> allObjects = new ArrayList<>();

	private Node addObject(Node n) {
		allObjects.add(n);
		if (completed)
			SwingUtilities.invokeLater(()->{
				renderNode((JmolShape3D)n);				
			});
		return n;
	}

	public BranchGroup createTextShape(String name, BranchGroup bg, String s, Point3d pos, float size,
			Font font, int align, int path, Point3d rotPoint, Appearance app) {
		Shape3D textShape = new JmolText(name, bg, s, font, align, path);
		addObject(textShape);

		Transform3D tsize = new Transform3D();
		tsize.set(size, new Vector3d(pos));

		Utils3d.setParents(textShape, new TransformGroup(tsize), bg);

		return bg;
	}

	public Node createTorus(String name, double innerRadius, double outerRadius, int innerFaces,
			int outerFaces, Appearance app) {
		return addObject(new JmolTorus(name, innerRadius, outerRadius, innerFaces, outerFaces, app));
	}

	public Node createBox(String name, double dx, double dy, double dz, Appearance app) {
		return addObject(new JmolBox(name, dx, dy, dz, app));
	}

	public Node createCylinder(String name, double radius, double height, boolean isHollow, int xdiv,
			int ydiv, Appearance app) {
		return addObject(new JmolCylinder(name, radius, height, isHollow, xdiv, ydiv, app));
	}

	public Node createSphere(String name, double radius, int divs, boolean isAtom, Appearance app) {
		return addObject(new JmolSphere(name, radius, divs, isAtom, app));
	}

	public TransformGroup createArrow(String name, TransformGroup tg, double radiusArrow, double lenArrow,
			double radius, float height, int precision, Appearance app) {
		tg.addChild(addObject(new JmolArrow(name, radiusArrow, lenArrow, radius, height, precision, app)));
		return tg;
	}

	public Node createQuad(String name, QuadArray quad, Appearance app) {
		return addObject(new JmolQuad(name, quad, app));
	}

	@Override
	public void reset(TransformGroup reset) {

	}

	@Override
	public void notifyRemove(Group parent, Node child) {
		//System.out.println("removed " + child.getName() + " from " + parent.getName());
		
		switch(parent.getName()) {
		case "root":
			this.mapRoot.remove(child.getName());
			break;
		}
		if (!completed)
			return;
		setShapeVisibility(child, false);
	}

	private void setShapeVisibility(Node child, boolean b) {
		JmolShape3D n = (JmolShape3D) (child instanceof Group ? Utils3d.getShapeChild((Group) child) : child);
		if (n == null)
			return;
		n.getDrawId();
		if (n.shape == null)
			return;
		n.shape.visible = b;
	}

	@Override
	public void notifyAdd(Group parent, Node child) {
		if (parent.getName() == null)
			System.out.println("???");
		switch(parent.getName()) {
		case "root":
			System.out.println("added " + child.getName() + " to " + parent.getName());
			this.mapRoot.put(child.getName(), child);
			break;
		}
		if (!completed)
			return;
		setShapeVisibility(child, true);
	}

	@Override
	public void notifyRemoveAll(Group g) {
		// TODO Auto-generated method stub

	}

	@Override
	public TransformGroup newTransformGroup(Transform3D t3d) {
		return (t3d == null ? new JmolTransformGroup() : new JmolTransformGroup(t3d));
	}

	public class JmolTransformGroup extends TransformGroup {

		public JmolTransformGroup() {
			super();
		}

		public JmolTransformGroup(Transform3D t3d) {
			super(t3d);		}

		private Transform3D tlast;

		/**
		 * Callback for transform changes. All underlying objects
		 * must be adjusted.
		 * 
		 */
		public void setTransform(Transform3D t3d) {
			if (!t3d.equals(tlast))
				super.setTransform(t3d);
			if (tlast == null) {
				tlast = new Transform3D();
			} else if (completed) {
				renderAllChilden(this);
			}
			tlast.set(t3d);
		}

		private void renderAllChilden(Group g) {
			Enumeration<Node> e = g.getAllChildren();
			while (e.hasMoreElements()) {
				Node n = e.nextElement();
				if (n instanceof JmolShape3D) {
					renderNode((JmolShape3D) n);
				} else if (n instanceof Group){
					renderAllChilden((Group) n);
				}
			}
		}
	}

	public void complete() {
		completed = true;
		String s = "background white;set history 0;set preservestate false;";
		for (int i = 0, n = allObjects.size(); i < n; i++) {
			Node node = allObjects.get(i);
			if (node instanceof JmolShape3D) {
				s += ((JmolShape3D) node).renderScript(this);
			}
		}
	//	System.out.println(s);
		viewer.scriptWait(s);
	}
	
    public void renderNode(Shape3D n) {
		String s = ((JmolShape3D) n).renderScript(this);
		//System.out.println(n + " "  + s);
		if (s.length() > 0)
			viewer.scriptWait(s);
	}

	@Override
	public Object getViewer() {
		return viewer;
	}

	private final static Transform3D t = new Transform3D();

	@Override
	public Transform3D getTransform(JmolShape3D shape) {
		Transform3D ret = t;
		ret.setIdentity();
		Node n = shape;
		Transform3D t = new Transform3D();// renderer.getTopTransform();
		while ((n = n.getParent()) != null) {
			if (n == root) {
				ret.mul(topTransform, ret);
				return ret;
			}
			if (n instanceof TransformGroup) {
				TransformGroup tg = (TransformGroup) n;
				tg.getTransform(t);
				ret.mul(t, ret);
			}
		}
		return null;
	}



}
