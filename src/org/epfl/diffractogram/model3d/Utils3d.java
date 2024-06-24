package org.epfl.diffractogram.model3d;

import java.awt.Font;
import java.awt.Label;
import java.awt.Point;
import java.awt.event.KeyEvent;

import javax.media.j3d.Appearance;
import javax.media.j3d.Behavior;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.epfl.diffractogram.util.WorldRenderer;

public class Utils3d {

	public static void changeCylinder(BranchGroup cyl, Point3d b, Point3d a) {
		TransformGroup tg = getTransformGroup(cyl);
		TransformGroup tgh = getTransformGroup(tg);
		Vector3f center = new Vector3f();
		Vector3f unit = new Vector3f();
		double height = calculateHeight(b, a, center, unit);
		createMatrix(tg, center, unit);
		Transform3D th = new Transform3D();
		th.set(new Matrix3d(1, 0, 0, 0, height, 0, 0, 0, 1));
		tgh.setTransform(th);
	}

	private static TransformGroup getTransformGroup(Group g) {
		for (int i = 0;i < 2; i++) {
			Node n = g.getChild(i);
			if (n instanceof TransformGroup)
				return (TransformGroup) n;
		}
		return null;
	}

	public static void changeCylinderApp(BranchGroup cyl, Appearance app) {
		getShapeChild(cyl).setAppearance(app);
	}

	public static BranchGroup createCylinder(Univers univers, String name, Point3d b, Point3d a, double radius, Appearance cylApp, int precision) {
		BranchGroup cylBg = new BranchGroup();
		cylBg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		cylBg.setCapability(BranchGroup.ALLOW_DETACH);

		Vector3f center = new Vector3f();
		Vector3f unit = new Vector3f();
		float height = calculateHeight(b, a, center, unit);
		TransformGroup tg = univers.newTransformGroup(null);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		createMatrix(tg, center, unit);
		Transform3D th = new Transform3D();
		th.set(new Matrix3d(1, 0, 0, 0, height, 0, 0, 0, 1));
		TransformGroup tgh = univers.newTransformGroup(th);
		tgh.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tgh.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		setParents(WorldRenderer.createCylinder(name + ":cyl", radius, 1, false, precision, 1, cylApp), tgh, tg, cylBg);
		cylBg.setName(name);
		return cylBg;
	}

	public static BranchGroup atom(Point3d p, Color3f color, double r) {
		return atom(p, createApp(color), r, 20);
	}

	public static int atomid;
	
	public static BranchGroup atom(Point3d p, Appearance app, double r, int facets) {
		BranchGroup bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		setParents(WorldRenderer.createSphere("atom:" + ++atomid, r, facets, true, app), getVectorTransformGroup(p.x, p.y, p.z, null), bg);
		return bg;
	}

	public static BranchGroup createArrow(String name, Point3d b, Point3d a, double radius, double radiusArrow, double lenArrow,
			Appearance cylApp, int precision) {
		Vector3f center = new Vector3f();
		Vector3f unit = new Vector3f();
		float height = calculateHeight(b, a, center, unit) - (float) lenArrow;
		Vector3d h = new Vector3d();
		h.sub(a, new Vector3d(center));
		h.normalize();
		h.scale(lenArrow / 2);
		center.sub(center, new Vector3f(h));
		TransformGroup tg = new TransformGroup();
		createMatrix(tg, center, unit);
		BranchGroup cylBg = new BranchGroup();
		cylBg.addChild(WorldRenderer.createArrow(name, tg, radiusArrow, lenArrow, radius, height, precision, cylApp));
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
	public static BranchGroup createLegend(String s, Point3d pos, Point3d rot, float size, Appearance app,
			boolean centered) {
		return WorldRenderer.getTextShape("text:" + s, new BranchGroup(), s, pos, size, new Font(null, Font.PLAIN, 2),
				centered ? Label.CENTER : Label.LEFT, KeyEvent.VK_RIGHT, rot, app);
	}

	public static BranchGroup createFixedLegend(String s, Point3d pos, float size, Appearance app, boolean centered) {
		return createLegend(s, pos, null, size, app, centered);
	}

//	public static Tuple3d mul(Tuple3d t, double scale) {
//		Tuple3d r = (Tuple3d) t.clone();
//		r.scale(scale);
//		return r;
//	}
	public static Vector3d mul(Vector3d t, double scale) {
		Vector3d r = (Vector3d) t.clone();
		r.scale(scale);
		return r;
	}

	public static Point3d mul(Point3d t, double scale) {
		Point3d r = (Point3d) t.clone();
		r.scale(scale);
		return r;
	}

	public static Vector3d norm(Vector3d t) {
		Vector3d r = new Vector3d(t);
		r.normalize();
		return r;
	}

	public static String posToString(double p) {
		return "" + Math.round(p * 100f) / 100f;
	}

	public static String posToString(Vector3d p) {
		return "(" + posToString(p.getX()) + " " + posToString(p.getY()) + " " + posToString(p.getZ()) + ")";
	}

	public static String posToString(Point3d p) {
		return "(" + posToString(p.getX()) + " " + posToString(p.getY()) + " " + posToString(p.getZ()) + ")";
	}

	public static String posToString(float[] p) {
		return posToString(new Point3d(p[0], p[1], p[2]));
	}

	public static String posToString(Point.Double p) {
		return "(" + posToString(p.x) + " " + posToString(p.y) + ")";
	}

	public static BranchGroup createRepere(Color3f colorText, Color3f colorArrows, Color3f colorCenter, String[] names,
			float sizeText, float sizeArrows, double deltaText, double deltaArrows, Vector3d x, Vector3d y,
			Vector3d z) {
		Appearance app1 = createApp(colorText);
		Appearance app2 = createApp(colorArrows);
		BranchGroup repere = new BranchGroup();

		if (colorCenter != null)
			repere.addChild(WorldRenderer.createSphere("axes:o" + names[0], .05, 10, false, createApp(colorCenter)));

		Point3d o = new Point3d(0, 0, 0);
		repere.addChild(createArrow("axes:" + names[0], o, new Point3d(mul(x, (x.length() + deltaArrows) / x.length())), sizeArrows,
				sizeArrows * 2f, sizeArrows * 6f, app2, 12));
		repere.addChild(createArrow("axes:" + names[1], o, new Point3d(mul(y, (y.length() + deltaArrows) / y.length())), sizeArrows,
				sizeArrows * 2f, sizeArrows * 6f, app2, 12));
		repere.addChild(createArrow("axes:" + names[2], o, new Point3d(mul(z, (z.length() + deltaArrows) / z.length())), sizeArrows,
				sizeArrows * 2f, sizeArrows * 6f, app2, 12));

		repere.addChild(createLegend(names[0], new Point3d(mul(x, (x.length() + deltaText) / x.length())), o, sizeText,
				app1, false));
		repere.addChild(createLegend(names[1], new Point3d(mul(y, (y.length() + deltaText) / y.length())), o, sizeText,
				app1, false));
		repere.addChild(createLegend(names[2], new Point3d(mul(z, (z.length() + deltaText) / z.length())), o, sizeText,
				app1, false));
		return repere;
	}

	public static BranchGroup createNamedVector(String name, Point3d p1, Point3d p2, Point3d p3, float size,
			Color3f colorText, Color3f colorArrow) {
		Appearance app1 = createApp(colorText);
		Appearance app2 = createApp(colorArrow);
		BranchGroup group = new BranchGroup();
		group.addChild(createArrow(name, p1, p2, .03 * size, .1 * size, .4 * size, app2, 12));
		group.addChild(createFixedLegend(name, p3, .15f * size, app1, true));
		return group;
	}

//	static final Color3f ambWhite = new Color3f(0.3f, 0.3f, 0.3f);
//	static final Color3f specular = new Color3f(1.0f, 1.0f, 1.0f);
//	static final Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
//	public static Appearance createApp(Color3f color) {
//		Appearance app = new Appearance();
//		Material mat = new Material(color, black, color, specular, 128);
//		mat.setLightingEnable(true);
//		app.setMaterial(mat);
//		return app;
//	}

	static final Color3f ambWhite = new Color3f(0.3f, 0.3f, 0.3f);
	static final Color3f specular = new Color3f(1.0f, 1.0f, 1.0f);
	static final Color3f black = new Color3f(0.0f, 0.0f, 0.0f);

	public static Appearance createApp(Color3f color) {
		Appearance app = newAppearance("color:" + color);
		Material mat = new Material(color, black, color, specular, 128);
		mat.setLightingEnable(true);
		app.setMaterial(mat);
		return app;
	}

	private static Color3f tc = new Color3f();
	public static Appearance createApp(Material mat, float transp) {
		Appearance app = new Appearance();
		app.setMaterial(mat);
		app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, transp));
		PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		pa.setBackFaceNormalFlip(false);
		app.setPolygonAttributes(pa);
		return app;
	}

	public static Appearance createApp(Color3f color, float transp) {
		Material mat = new Material(color, black, color, specular, 128);
		mat.setLightingEnable(true);
		Appearance a = createApp(mat, transp);
		a.setName("color:" + color + "t=" + transp);
		return a;
	}

	public static Shape3D createCircle(double radius, int resolution, Color4f c) {
		int length = resolution + 1;
		int ops = 1 | 12;// GeometryArray.COORDINATES|GeometryArray.COLOR_4;
		LineStripArray lsa = new LineStripArray(length, ops, new int[] { length });
		// first and last points
		Point3d pt0 = new Point3d(radius, 0, 0);
		lsa.setCoordinate(0, pt0);
		if (c != null)
			lsa.setColor(0, c);
		// computed points
		Point3f pt = new Point3f();
		for (int i = 1; i < 32; i++) {
			pt.x = (float) (radius * Math.cos(i * Math.PI / 16));
			pt.y = (float) (radius * Math.sin(i * Math.PI / 16));
			lsa.setCoordinate(i, pt);
			if (c != null)
				lsa.setColor(i, c);
		}
		lsa.setCoordinate(32, pt0);
		if (c != null)
			lsa.setColor(32, c);
		return new Shape3D(lsa);
	}

	private static float calculateHeight(Point3d b, Point3d a, Vector3f center, Vector3f unit) {
		Vector3f base = new Vector3f(b);
		Vector3f apex = new Vector3f(a);

		// calculate center of object
		center.x = (apex.x - base.x) / 2 + base.x;
		center.y = (apex.y - base.y) / 2 + base.y;
		center.z = (apex.z - base.z) / 2 + base.z;

		// calculate height of object and unit vector along cylinder axis
		unit.sub(apex, base); // unit = apex - base;
		float height = unit.length();
		unit.normalize();
		return height;
	}

	private static void createMatrix(TransformGroup tgOut, Vector3f center, Vector3f unit) {
		/*
		 * A Java3D cylinder is created lying on the Y axis by default. The idea here is
		 * to take the desired cylinder's orientation and perform a tranformation on it
		 * to get it ONTO the Y axis. Then this transformation matrix is inverted and
		 * used on a newly-instantiated Java 3D cylinder.
		 */

		// calculate vectors for rotation matrix
		// rotate object in any orientation, onto Y axis (exception handled below)
		// (see page 418 of Computer Graphics by Hearn and Baker)
		Vector3f uX = new Vector3f();
		Vector3f uY = new Vector3f();
		Vector3f uZ = new Vector3f();
		float magX;
		Transform3D rotateFix = new Transform3D();

		uY = new Vector3f(unit);
		uX.cross(unit, new Vector3f(0, 0, 1));
		magX = uX.length();
		// magX == 0 if object's axis is parallel to Z axis
		if (magX != 0) {
			uX.z = uX.z / magX;
			uX.x = uX.x / magX;
			uX.y = uX.y / magX;
			uZ.cross(uX, uY);
		} else {
			// formula doesn't work if object's axis is parallel to Z axis
			// so rotate object onto X axis first, then back to Y at end
			float magZ;
			// (switched z -> y, y -> x, x -> z from code above)
			uX = new Vector3f(unit);
			uZ.cross(unit, new Vector3f(0, 1, 0));
			magZ = uZ.length();
			uZ.x = uZ.x / magZ;
			uZ.y = uZ.y / magZ;
			uZ.z = uZ.z / magZ;
			uY.cross(uZ, uX);
			// rotate object 90 degrees CCW around Z axis--from X onto Y
			rotateFix.rotZ(-Math.PI / 2.0);
		}

		// create the rotation matrix
		Transform3D transMatrix = new Transform3D();
		Transform3D rotateMatrix = new Transform3D(
				new Matrix4f(uX.x, uX.y, uX.z, 0, uY.x, uY.y, uY.z, 0, uZ.x, uZ.y, uZ.z, 0, 0, 0, 0, 1));
		// invert the matrix; need to rotate it off of the Z axis
		rotateMatrix.invert();
		// rotate the cylinder into correct orientation
		transMatrix.mul(rotateMatrix);
		transMatrix.mul(rotateFix);
		// translate the cylinder away
		transMatrix.setTranslation(center);
		// create the transform group

		tgOut.setTransform(transMatrix);

		// TransformGroup tg = univers.newTransformGroup(transMatrix);
		// return tg;
	}

	/**
	 * Safely add parents to a node as node child of g[0] child of g[1] ...
	 * 
	 * @param node
	 * @param groups
	 */
	public static void setParents(Node node, Group... groups) {
		for (int i = 0; i < groups.length; i++) {
			if (groups[i] != node.getParent())
				groups[i].addChild(node);
			node = groups[i];
		}
	}

	public static TransformGroup getVectorTransformGroup(double x, double y, double z, Transform3D t) {
		if (t == null)
			t = new Transform3D();
		t.set(new Vector3d(x, y, z));
		return new TransformGroup(t);
	}

	public static Shape3D getShapeChild(Group a) {
		Node g = a;
		while (!(g instanceof Shape3D)) {
			Node c = ((Group) g).getChild(0);
			if (c instanceof Behavior) {
				g = ((Group) g).getChild(1);
			} else {
				g = c;
			}
		}
		return (Shape3D) g;
	}

	public static Appearance newAppearance(String name) {
		Appearance a = new Appearance();
		a.setName(name);
		return a;
	}

}
