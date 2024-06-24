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
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import org.epfl.diffractogram.diffrac.DefaultValues;
import org.epfl.diffractogram.model3d.Univers;

import j3djmol.JmolWorldRenderer;

public abstract class WorldRenderer {
	
	protected Univers univers;
	protected JPanel panel3d;
	
	protected boolean completed;
	
	protected boolean debugging = true;

	public static boolean isJS = (/** @j2sNative true || */
	false);

	
	public static boolean isJmol;
	
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

	protected void applyTransform(Transform3D t3d) {
		univers.applyTransform(t3d);
	}

	public static BranchGroup getTextShape(String name, BranchGroup bg, String s, Point3d pos, float size, Font font, int align, int path, Point3d rotPoint,
			Appearance app) {
		return (isJmol ? JmolWorldRenderer.getTextShapeImpl(name, bg, s, pos, size, font, align, path, rotPoint, app)
				: Java3DWorldRenderer.getTextShapeImpl(name, bg, s, pos, size, font, align, path, rotPoint, app));
	}

	public static Node createBox(String name, double dx, double dy, double dz, Appearance app) {
		return (isJmol ? JmolWorldRenderer.createBoxImpl(name, dx, dy, dz, app)
				: Java3DWorldRenderer.createBoxImpl(name, dx, dy, dz, app));
	}

	public static Node createCylinder(String name, double radius, double height, boolean isHollow, int xdiv, int ydiv, Appearance app) {
		return (isJmol ? JmolWorldRenderer.createCylinderImpl(name, radius, height, isHollow, xdiv, ydiv, app)
				: Java3DWorldRenderer.createCylinderImpl(name, radius, height, isHollow, xdiv, ydiv, app));
	}

	public static TransformGroup createArrow(String name, TransformGroup tg, double radiusArrow, double lenArrow, double radius, float height,
			int precision, Appearance app) {
		return (isJmol ? JmolWorldRenderer.createArrowImpl(name, tg, radiusArrow, lenArrow, radius, height, precision, app)  
				: Java3DWorldRenderer.createArrowImpl(name, tg, radiusArrow, lenArrow, radius, height, precision, app));
	}

	public static Node createSphere(String name, double radius, int divs, boolean isAtom, Appearance app) {
		return (isJmol ? JmolWorldRenderer.createSphereImpl(name, radius, divs, isAtom, app)
				: Java3DWorldRenderer.createSphereImpl(name, radius, divs, app));
	}

	public static Node createTorus(String name, double innerRadius, double outerRadius, int innerFaces, int outerFaces, Appearance app) {
		return (isJmol ? JmolWorldRenderer.createTorusImpl(name, innerRadius, outerRadius, innerFaces, outerFaces, app)
				: Java3DWorldRenderer.createTorusImpl(name, innerRadius, outerRadius, innerFaces, outerFaces, app));
	}

	public abstract void reset(TransformGroup reset);

	public abstract void notifyRemove(Group parent, Node child);

	public abstract void notifyAdd(Group parent, Node child);

	public abstract void notifyRemoveAll(Group g);

	
	protected Map<String, Node> mapRoot = new HashMap<>();

	public void complete() {
		completed = true;
		if (debugging) {
		for (Entry<String, Node> en:mapRoot.entrySet()) {
			dumpRoot("root." + en.getKey(), en.getValue());
		}
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
	
}