package org.epfl.diffractogram.util;

import java.awt.Font;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import org.epfl.diffractogram.model3d.Univers;

public abstract class WorldRenderer {
	
	protected Univers univers;
	protected JPanel panel3d;
	
	public static boolean isJS = (/** @j2sNative true || */
	false);

	public WorldRenderer(JPanel panel3d, Univers univers) {
		this.panel3d = panel3d;
		this.univers = univers;
	}

	public static WorldRenderer createWorldRenderer(JPanel panel3d, Univers univers) {
		return (isJS ? new JSWorldRenderer(panel3d, univers) : new Java3DWorldRenderer(panel3d, univers));
	}

	public abstract void setEnvironment(TransformGroup reset, BranchGroup root);

	public abstract boolean isParallel();

	public abstract void setParallel(boolean b);

	public abstract void setBackgroundColor(Color3f bgColor);

	public abstract void cleanup();

	protected void applyTransform(Transform3D t3d) {
		univers.applyTransform(t3d);
	}

	public static BranchGroup getTextShape(BranchGroup bg, String s, Point3d pos, float size, Font font, int align, int path, Point3d rotPoint,
			Appearance app) {
		return (isJS ? JSWorldRenderer.getTextShapeImpl(bg, s, pos, size, font, align, path, rotPoint, app)
				: Java3DWorldRenderer.getTextShapeImpl(bg, s, pos, size, font, align, path, rotPoint, app));
	}

	public static Node createBox(double dx, double dy, double dz, Appearance app) {
		return (isJS ? JSWorldRenderer.createBoxImpl(dx, dy, dz, app)
				: Java3DWorldRenderer.createBoxImpl(dx, dy, dz, app));
	}

	public static Node createCylinder(double radius, double height, boolean isHollow, int xdiv, int ydiv, Appearance app) {
		return (isJS ? JSWorldRenderer.createCylinderImpl(radius, height, isHollow, xdiv, ydiv, app)
				: Java3DWorldRenderer.createCylinderImpl(radius, height, isHollow, xdiv, ydiv, app));
	}

	public static TransformGroup createArrow(TransformGroup tg, double radiusArrow, double lenArrow, double radius, float height,
			int precision, Appearance app) {
		return (isJS ? JSWorldRenderer.createArrowImpl(tg, radiusArrow, lenArrow, radius, height, precision, app)  
				: Java3DWorldRenderer.createArrowImpl(tg, radiusArrow, lenArrow, radius, height, precision, app));
	}

	public static Node createSphere(double radius, int divs, boolean isAtom, Appearance app) {
		return (isJS ? JSWorldRenderer.createSphereImpl(radius, divs, isAtom, app)
				: Java3DWorldRenderer.createSphereImpl(radius, divs, app));
	}

	public static Node createTorus(double innerRadius, double outerRadius, int innerFaces, int outerFaces, Appearance app) {
		return (isJS ? JSWorldRenderer.createTorusImpl(innerRadius, outerRadius, innerFaces, outerFaces, app)
				: Java3DWorldRenderer.createTorusImpl(innerRadius, outerRadius, innerFaces, outerFaces, app));
	}

}