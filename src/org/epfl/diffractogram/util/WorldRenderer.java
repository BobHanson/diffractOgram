package org.epfl.diffractogram.util;

import java.awt.Font;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import org.epfl.diffractogram.diffrac.DefaultValues;
import org.epfl.diffractogram.model3d.Univers;

import org.jmol.j3d.JmolWorldRenderer;

public abstract class WorldRenderer {
	
	protected Univers univers;
	protected JPanel panel3d;
	
	public boolean completed;
	
	protected boolean debugging = true;

	public static boolean isJS = (/** @j2sNative true || */
	false);

	
	public static boolean isJmol;
	
	protected Map<String, Node> mapRoot = new HashMap<>();
	public BranchGroup root;
	public Transform3D topTransform;

	public WorldRenderer(JPanel panel3d, Univers univers) {
		this.panel3d = panel3d;
		this.univers = univers;
	}

	public static WorldRenderer createWorldRenderer(JPanel panel3d, Univers univers) {
		isJmol = DefaultValues.useJmol;
		return (isJmol ? new JmolWorldRenderer(panel3d, univers) : new Java3DWorldRenderer(panel3d, univers));
	}

	public abstract void setEnvironment(TransformGroup reset);

	public abstract boolean isParallel();

	public abstract void setParallel(boolean b);

	public abstract void setBackgroundColor(Color3f bgColor);

	public abstract void cleanup();

	public abstract Node createBox(String name, double dx, double dy, double dz, Appearance app);
	
	public abstract Node createCylinder(String name, double radius, double height, boolean isHollow, int xdiv, int ydiv, Appearance app);
	
	public abstract TransformGroup createArrow(String name, TransformGroup tg, double radiusArrow, double lenArrow, double radius, float height,
			int precision, Appearance app);
	
	public abstract Node createSphere(String name, double radius, int divs, boolean isAtom, Appearance app);
	
	public abstract BranchGroup createTextShape(String name, BranchGroup bg, String s, Point3d pos, float size, Font font, int align, int path, Point3d rotPoint,
			Appearance app);
	
	public abstract Node createTorus(String name, double innerRadius, double outerRadius, int innerFaces, int outerFaces, Appearance app);

	public abstract Node createQuad(String name, QuadArray quad, Appearance app);
	
	public abstract void reset(TransformGroup reset);

	public abstract void notifyRemove(Group parent, Node child);

	public abstract void notifyAdd(Group parent, Node child);

	public abstract void notifyRemoveAll(Group g);
	
	public void complete() {
		completed = true;
		if (debugging) {
			for (Entry<String, Node> en : mapRoot.entrySet()) {
				Node node = en.getValue();
				dumpRoot("root." + en.getKey(), node);
			}
		} else {
			System.out.println("WR complete");
		}
	}

	private void dumpRoot(String key, Node value) {
		String name = value.getName();
		if (name != null) {
			System.out.println(key);
		}
		if (value instanceof Group) {
			Group g = (Group) value;
			Enumeration<? extends Node> e = g.getAllChildren();
			while (e.hasMoreElements()) {
				Node n = e.nextElement();
				String ename = n.getName();
				dumpRoot(ename == null ? key  : key + "." + ename, n);
			}
		}
	}

	public abstract TransformGroup newTransformGroup(Transform3D t3d);

	public BranchGroup getRootBranchGroup() {
		return root = new BranchGroup();
	}

	public void setTopTransform(TransformGroup tgTop) {
		Transform3D t = new Transform3D();
		tgTop.getTransform(t);
		topTransform = t;
	}


}