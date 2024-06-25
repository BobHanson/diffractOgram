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
import javax.media.j3d.QuadArray;
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
import javax.vecmath.TexCoord2f;
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

	public static void changeCylinderApp(BranchGroup cyl, Appearance app) {
		getShapeChild(cyl).setAppearance(app);
	}

	public static TransformGroup getTransformGroup(Group g) {
		for (int i = 0;i < 2; i++) {
			Node n = g.getChild(i);
			if (n instanceof TransformGroup)
				return (TransformGroup) n;
		}
		return null;
	}


//	public  static Tuple3d mul(Tuple3d t, double scale) {
//		Tuple3d r = (Tuple3d) t.clone();
//		r.scale(scale);
//		return r;
//	}
	public  static Vector3d mul(Vector3d t, double scale) {
		Vector3d r = (Vector3d) t.clone();
		r.scale(scale);
		return r;
	}

	public  static Point3d mul(Point3d t, double scale) {
		Point3d r = (Point3d) t.clone();
		r.scale(scale);
		return r;
	}

	public  static Vector3d norm(Vector3d t) {
		Vector3d r = new Vector3d(t);
		r.normalize();
		return r;
	}

	public  static String posToString(double p) {
		return "" + Math.round(p * 100f) / 100f;
	}

	public  static String posToString(Vector3d p) {
		return "(" + posToString(p.getX()) + " " + posToString(p.getY()) + " " + posToString(p.getZ()) + ")";
	}

	public  static String posToString(Point3d p) {
		return "(" + posToString(p.getX()) + " " + posToString(p.getY()) + " " + posToString(p.getZ()) + ")";
	}

	public  static String posToString(float[] p) {
		return posToString(new Point3d(p[0], p[1], p[2]));
	}

	public  static String posToString(Point.Double p) {
		return "(" + posToString(p.x) + " " + posToString(p.y) + ")";
	}

//	public static final Color3f ambWhite = new Color3f(0.3f, 0.3f, 0.3f);
//	public static final Color3f specular = new Color3f(1.0f, 1.0f, 1.0f);
//	public static final Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
//	public  static Appearance createApp(Color3f color) {
//		Appearance app = new Appearance();
//		Material mat = new Material(color, black, color, specular, 128);
//		mat.setLightingEnable(true);
//		app.setMaterial(mat);
//		return app;
//	}

	public static final Color3f ambWhite = new Color3f(0.3f, 0.3f, 0.3f);
	public static final Color3f specular = new Color3f(1.0f, 1.0f, 1.0f);
	public static final Color3f black = new Color3f(0.0f, 0.0f, 0.0f);

	public  static Appearance createApp(Color3f color) {
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

	public  static Appearance createApp(Color3f color, float transp) {
		Material mat = new Material(color, black, color, specular, 128);
		mat.setLightingEnable(true);
		Appearance a = createApp(mat, transp);
		a.setName("color:" + color + "t=" + transp);
		return a;
	}

	public  static Shape3D createCircle(double radius, int resolution, Color4f c) {
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

	public static float calculateHeight(Point3d b, Point3d a, Vector3f center, Vector3f unit) {
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

	public  static void createMatrix(TransformGroup tgOut, Vector3f center, Vector3f unit) {
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
	public  static void setParents(Node node, Group... groups) {
		for (int i = 0; i < groups.length; i++) {
			if (groups[i] != node.getParent())
				groups[i].addChild(node);
			node = groups[i];
		}
	}

	public  static TransformGroup getVectorTransformGroup(double x, double y, double z, Transform3D t) {
		if (t == null)
			t = new Transform3D();
		t.set(new Vector3d(x, y, z));
		return new TransformGroup(t);
	}

	public  static Shape3D getShapeChild(Group a) {
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

	public  static Appearance newAppearance(String name) {
		Appearance a = new Appearance();
		a.setName(name);
		return a;
	}

	public  static QuadArray createQuad() {
		QuadArray quad = new QuadArray(4,
				QuadArray.COORDINATES | QuadArray.TEXTURE_COORDINATE_2 | QuadArray.NORMALS);
		quad.setCoordinate(0, new Point3d(-.5, 0, -.5));
		quad.setCoordinate(1, new Point3d(-.5, 0, +.5));
		quad.setCoordinate(2, new Point3d(+.5, 0, +.5));
		quad.setCoordinate(3, new Point3d(+.5, 0, -.5));

		quad.setNormal(0, new Vector3f(0, 1, 0));
		quad.setNormal(1, new Vector3f(0, 1, 0));
		quad.setNormal(2, new Vector3f(0, 1, 0));
		quad.setNormal(3, new Vector3f(0, 1, 0));

		quad.setTextureCoordinate(0, 0, new TexCoord2f(0.0f, 0.0f));
		quad.setTextureCoordinate(0, 3, new TexCoord2f(1.0f, 0.0f));
		quad.setTextureCoordinate(0, 2, new TexCoord2f(1.0f, -1.0f));
		quad.setTextureCoordinate(0, 1, new TexCoord2f(0.0f, -1.0f));
		return quad;
	}

	public  static QuadArray createQuad(Vector3d e1, Vector3d e2, Vector3d e3, double w, double h) {
		Matrix3d m = new Matrix3d();
		m.setColumn(0, e1);
		m.setColumn(1, e2);
		m.setColumn(2, e3);

		Point3d p1 = new Point3d(-w / 2, 0, -h / 2);
		Point3d p2 = new Point3d(-w / 2, 0, +h / 2);
		Point3d p3 = new Point3d(+w / 2, 0, +h / 2);
		Point3d p4 = new Point3d(+w / 2, 0, -h / 2);
		m.transform(p1);
		m.transform(p2);
		m.transform(p3);
		m.transform(p4);

		QuadArray quad = new QuadArray(4, QuadArray.COORDINATES | QuadArray.NORMALS);
		quad.setCoordinate(0, p1);
		quad.setCoordinate(1, p2);
		quad.setCoordinate(2, p3);
		quad.setCoordinate(3, p4);

		Vector3f e2f = new Vector3f(e2);
		quad.setNormal(0, e2f);
		quad.setNormal(1, e2f);
		quad.setNormal(2, e2f);
		quad.setNormal(3, e2f);
		return quad;
	}


}
