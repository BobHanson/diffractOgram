package org.epfl.diffractogram.util;

import java.awt.AWTEvent;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Enumeration;

import javax.media.j3d.PickRay;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;

import org.epfl.diffractogram.model3d.Univers.Selectable;

public class Java3dUtil {

	public static class TriangleStripArray extends javax.media.j3d.TriangleStripArray {

		public TriangleStripArray(int vertexCount, int format, int[] stripCounts) {
			super(vertexCount, format, stripCounts);
		}

	}

	public static class AmbientLight extends javax.media.j3d.AmbientLight {

		public AmbientLight(Color3f c) {
			super(c);
		}
	}

	public static class Background extends javax.media.j3d.Background {

		public Background(Color3f c) {
			super(c);
		}
	}

	public static abstract class Behavior extends javax.media.j3d.Behavior {

		private javax.media.j3d.WakeupOr mouseCriterion;

		@Override
		public void initialize() {
			WakeupCriterion[] mouseEvents = new WakeupCriterion[3];
			mouseEvents[0] = new javax.media.j3d.WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED);
			mouseEvents[1] = new javax.media.j3d.WakeupOnAWTEvent(MouseEvent.MOUSE_DRAGGED);
			mouseEvents[2] = new javax.media.j3d.WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED);
			mouseCriterion = new javax.media.j3d.WakeupOr(mouseEvents);
			wakeupOn(mouseCriterion);
		}

		protected abstract void processMouseEvent(MouseEvent e);
		
		@SuppressWarnings("rawtypes")
		@Override
		public void processStimulus(Enumeration criteria) {
			javax.media.j3d.WakeupCriterion wakeup;
			while (criteria.hasMoreElements()) {
				wakeup = (WakeupCriterion) criteria.nextElement();
				if (wakeup instanceof WakeupOnAWTEvent) {
					AWTEvent[] events = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
					for (int i = 0; i < events.length; i++) {
						if (events[i] instanceof MouseEvent) {
							processMouseEvent((MouseEvent) events[i]);
						}
					}
				}
			}
			wakeupOn(mouseCriterion);
		}

		public Selectable getPointedObject(BranchGroup root, Point3d mousePos, Vector3d mouseVec) {
			PickRay pickRay = new PickRay();
			pickRay.set(mousePos, mouseVec);
			javax.media.j3d.SceneGraphPath[] sceneGraphPath = root.pickAllSorted(pickRay);
			if (sceneGraphPath != null) {
				for (int j = 0; j < sceneGraphPath.length; j++) {
					if (sceneGraphPath[j] != null) {
						javax.media.j3d.Node node = sceneGraphPath[j].getObject();
						if (node instanceof Shape3D) {
							try {
								double dist[] = { 0.0 };
								boolean isRealHit = ((Shape3D) node).intersect(sceneGraphPath[j], pickRay, dist);
								if (isRealHit) {
									Object userData = node.getUserData();
									if (userData != null && userData instanceof Selectable) {
										return (Selectable) userData;
									}
								}
							} catch (Exception e) {
							}
						}
					}
				}
			}
			return null;
		}
		
	}

	public static class BoundingLeaf extends javax.media.j3d.BoundingLeaf {

		public BoundingLeaf(BoundingSphere bounds) {
			super(bounds);
		}
	}

	public static class BoundingSphere extends javax.media.j3d.BoundingSphere {

		public BoundingSphere(Point3d p, double d) {
			super(p, d);
		}
	}

	@SuppressWarnings("serial")
	public static class Canvas3D extends javax.media.j3d.Canvas3D {

		public Canvas3D(GraphicsConfiguration preferredConfiguration) {
			super(preferredConfiguration);
		}
	}

	public static class DirectionalLight extends javax.media.j3d.DirectionalLight {

		public DirectionalLight(Color3f light1Color, Vector3f light1Direction) {
			super(light1Color, light1Direction);
		}
	}

	public static class Node extends javax.media.j3d.Node {
	}

	public static class SceneGraphPath extends javax.media.j3d.SceneGraphPath {
	}

	public static class View extends javax.media.j3d.View {
	}

	public static class Box extends com.sun.j3d.utils.geometry.Box {

		public Box(float x, float y, float z, Appearance a) {
			super(x, y, z, a);
		}

	}

	public static class Cone extends com.sun.j3d.utils.geometry.Cone {

		public Cone(float radiusArrow, float lenArrow, int generateNormals, int precision, int i, Appearance a) {
			super(radiusArrow, lenArrow, generateNormals, precision, i, a);
		}

	}

	public static class Sphere extends com.sun.j3d.utils.geometry.Sphere {

		public Sphere(float dotSize3d, int generateNormals, int i, Appearance a) {
			super(dotSize3d, generateNormals, i, a);
		}

	}

	public static class Cylinder extends com.sun.j3d.utils.geometry.Cylinder {

		public Cylinder(float f, float g, int generateNormals, int i, int j, Appearance a) {
			super(f, g, generateNormals, i, j, a);
		}

	}

	public static class SimpleUniverse extends com.sun.j3d.utils.universe.SimpleUniverse {

		public SimpleUniverse(Canvas3D canvas3d) {
			super(canvas3d);
		}

		public void setViewingTransform() {
			getViewingPlatform().setNominalViewingTransform();
		}

	}

	public static class Torus extends org.j3d.geom.Torus {

		public Torus(float f, float g, int i, int j, Appearance a) {
			super(f, g, i, j, a);
		}
		
	}

	@SuppressWarnings("serial")
	public static class TexCoord2f extends javax.vecmath.TexCoord2f {

		public TexCoord2f(float f, float g) {
			super(f, g);
		}

	}

	@SuppressWarnings("serial")
	public static class Point3d extends javax.vecmath.Point3d {

		public Point3d() {
			super();
		}

		public Point3d(double x, double y, double z) {
			super(x, y, z);
		}

		public Point3d(Point3d v) {
			super(v);
		}

		public Point3d(Vector3d v) {
			super(v);
		}

		public void add(Vector3d v) {
			super.add(v);
		}

	}

	@SuppressWarnings("serial")
	public static class Vector2d extends javax.vecmath.Vector2d {

		public Vector2d(double x, double y) {
			super(x, y);
		}
	}

	@SuppressWarnings("serial")
	public static class Point2f extends javax.vecmath.Point2f {

		public Point2f() {
			super();
		}
	}

	@SuppressWarnings("serial")
	public static class Point3f extends javax.vecmath.Point3f {

		public Point3f() {
			super();
		}

		public Point3f(float x, float y, float z) {
			super(x, y, z);
		}

		public Point3f(Point3f v) {
			super(v);
		}

		public Point3f(Point3d rot) {
			super(rot);
		}

	}

	@SuppressWarnings("serial")
	public static class Matrix3d extends javax.vecmath.Matrix3d {

		public Matrix3d(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21,
				double m22) {
			super(m00, m01, m02, m10, m11, m12, m20, m21, m22);
		}

		public Matrix3d() {
			super();
		}

		public void setColumn(int i, Vector3d v) {
			super.setColumn(i, v);
		}

		public void transform(Point3d p) {
			super.transform(p);
		}

	}

	@SuppressWarnings("serial")
	public static class Matrix4f extends javax.vecmath.Matrix4f {

		public Matrix4f(float x, float y, float z, float i, float x2, float y2, float z2, float j, float x3, float y3,
				float z3, float k, float l, float m, float n, float o) {
			super(x, y, z, i, x2, y2, z2, j, x3, y3, z3, k, l, m, n, o);
		}
	}

	@SuppressWarnings("serial")
	public static class Tuple3d extends javax.vecmath.Tuple3d {

	}

	@SuppressWarnings("serial")
	public static class Vector3d extends javax.vecmath.Vector3d {

		public Vector3d() {
			super();
		}

		public Vector3d(double x, double y, double z) {
			super(x, y, z);
		}

		public Vector3d(Point3d center) {
			super(center);
		}

		public Vector3d(Vector3f center) {
			super(center);
		}

		public Vector3d(Vector3d t) {
			super(t);
		}

		public void add(Vector3d x, Vector3d y) {
			super.add(x, y);
		}

		public void scaleBy(double d) {
			super.scale(d);
		}

		public void add(Vector3d z) {
			super.add(z);
		}

	}

	@SuppressWarnings("serial")
	public static class Vector3f extends javax.vecmath.Vector3f {

		public Vector3f() {
			super();
		}

		public Vector3f(float x, float y, float z) {
			super(x, y, z);
		}

		public Vector3f(Vector3d v) {
			super(v);
		}

		public Vector3f(Point3d b) {
			super(b);
		}

		public Vector3f(Vector3f unit) {
			super(unit);
		}

	}

	@SuppressWarnings("serial")
	public static class Color3f extends javax.vecmath.Color3f {
		public Color3f(float r, float g, float b) {
			super(r, g, b);
		}

	}

	@SuppressWarnings("serial")
	public static class Color4f extends javax.vecmath.Color4f {
		public Color4f(float a, float r, float g, float b) {
			super(a, r, g, b);
		}

	}

	public static class Appearance extends javax.media.j3d.Appearance {
	}

	public static class BranchGroup extends javax.media.j3d.BranchGroup {

	}

	public static class Font3D extends javax.media.j3d.Font3D {

		public Font3D(Font font, FontExtrusion fontExtrusion) {
			super(font, fontExtrusion);
		}
	}

	public static class FontExtrusion extends javax.media.j3d.FontExtrusion {
	}

	public static class ImageComponent2D extends javax.media.j3d.ImageComponent2D {

		public ImageComponent2D(int formatRgba, BufferedImage i, boolean b, boolean c) {
			super(formatRgba, i, b, c);
		}
	}

	public static class LineStripArray extends javax.media.j3d.LineStripArray {

		public LineStripArray(int length, int i, int[] js) {
			super(length, i, js);
		}
	}

	public static class Material extends javax.media.j3d.Material {

		public Material(Color3f a, Color3f b, Color3f c, Color3f d, float trans) {
			super(a, b, c, d, trans);
		}
	}

	public static class OrientedShape3D extends javax.media.j3d.OrientedShape3D {
	}

	public static class PolygonAttributes extends javax.media.j3d.PolygonAttributes {
	}

	public static class QuadArray extends javax.media.j3d.QuadArray {

		public QuadArray(int i, int j) {
			super(i, j);
		}
	}

	public static class Shape3D extends javax.media.j3d.Shape3D {

		public Shape3D(QuadArray q, Appearance a) {
			super(q, a);
		}

		public Shape3D() {
			super();
		}

		public Shape3D(LineStripArray lsa) {
			super(lsa);
		}
	}

	public static class Text3D extends javax.media.j3d.Text3D {

		public Text3D(Font3D f3d, String s, Point3f p, int i, int pathRight) {
			super(f3d, s, p, i, pathRight);
		}
	}

	public static class Texture extends javax.media.j3d.Texture {
	}

	public static class Texture2D extends javax.media.j3d.Texture2D {

		public Texture2D(int baseLevel, int rgba, int textureWidth, int textureHeight) {
			super(baseLevel, rgba, textureWidth, textureHeight);
		}
	}

	public static class TextureAttributes extends javax.media.j3d.TextureAttributes {
	}

	public static class TransparencyAttributes extends javax.media.j3d.TransparencyAttributes {

		public TransparencyAttributes(int nicest, float trans) {
			super(nicest, trans);
		}
	}

	public static class Transform3D extends javax.media.j3d.Transform3D {

		public Transform3D(Matrix4f m) {
			super(m);
		}

		public Transform3D() {
			super();
		}

		public void transform(Point3d p) {
			super.transform(p);
		}

		public void transform(Vector3d v) {
			super.transform(v);
		}

		public void mul(Transform3D t3v2) {
			super.mul(t3v2);
		}

		public void mul(Transform3D t1, Transform3D t2) {
			super.mul(t1, t2);
		}

		public void rotZ(double d) {
			super.rotZ(d);
		}

		public void rotY(double d) {
			super.rotY(d);
		}

		public void rotX(double d) {
			super.rotX(d);
		}

		public void invert(Transform3D t3d) {
			super.invert(t3d);
		}

		public void set(Matrix3d m) {
			super.set(m);
		}

		public void set(Vector3d v) {
			super.set(v);
		}

	}

	public static class TransformGroup extends javax.media.j3d.TransformGroup {

		public TransformGroup(Transform3D t) {
			super(t);
		}

		public TransformGroup() {
			super();
		}

		public void setTransform(Transform3D t3d) {
			super.setTransform(t3d);
		}

		public void addChild(TransformGroup l) {
			super.addChild(l);
		}

		public void setCapabilityTo(int p) {
			super.setCapability(p);
		}

	}
	
	

}
