package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;

import org.epfl.diffractogram.jmol.JmolWorldRenderer;
import org.jmol.j3d.JmolWorldRendererI;
import org.jmol.modelset.Atom;
import org.jmol.script.T;
import org.jmol.shape.Mesh;
import org.jmol.shapespecial.Draw;
import org.jmol.shapesurface.Isosurface;
import org.jmol.util.C;
import org.jmol.viewer.JC;
import org.jmol.viewer.Viewer;

import javajs.util.CU;
import javajs.util.M4d;
import javajs.util.P3d;
import javajs.util.T3d;

/**
 * An abstract class serving as a basis for replacements to Java3D renderable
 * objects.
 * 
 * Subclassed by JmolArrow, JmolBox, JmolCylinder, JmolQuad, JmolSphere,
 * JmolText, and JmolTorus
 * 
 * @author hansonr@stolaf.edu
 *
 */
public abstract class JmolShape3D extends Shape3D {

	public final static int JMOL_SHAPE_SPHERE = 1;
	public final static int JMOL_SHAPE_ATOM = 2;
	public final static int JMOL_SHAPE_CYLINDER = 3;
	public final static int JMOL_SHAPE_ARROW = 4;
	public final static int JMOL_SHAPE_BOX = 5;
	public final static int JMOL_SHAPE_TORUS = 6;
	public final static int JMOL_SHAPE_TEXT = 7;
	public final static int JMOL_SHAPE_PANEL = 8;
	public final static int JMOL_SHAPE_VERTEX_SET = 9;
	public final static int JMOL_SHAPE_UNITCELL = 10;

	protected Point3d[] vertices;

	protected P3d[] jmolVertices;

	/**
	 * JMOL_SHAPE_xxx; for reference only, not used in rendering
	 */
	int type;
	
	/**
	 * number or isosurfaces involved; may be 0 if DRAW is used instead
	 */
	int isosurfaceCount;

	protected JmolWorldRendererI renderer;

	protected String thisID;

	protected Atom atom; // not used

	protected Mesh shape;

	protected Mesh[] shapes;

	protected final static Point3d ptemp = new Point3d();

	protected final static P3d pt = new P3d();

	protected final static Color3f j3dColor = new Color3f();

	protected int argb;
	
	protected float translucency;

	protected final static Transform3D t = new Transform3D();

	public abstract String renderScript(JmolWorldRendererI renderer);

	JmolShape3D(String name, Appearance app, int type) {
		this.type = type; // not used; for debugging only
		setCapability(ALLOW_APPEARANCE_WRITE);
		setName(name);
		setAppearance(app);
	}

	public void setRenderer(JmolWorldRenderer renderer) {
		this.renderer = renderer;
	}

	public void setJmolShapeVisibility(boolean b) {
		getThisID();
		if (shape != null) {
			shape.visible = b;
			if (thisID.endsWith(":")) {
				System.out.println("removing all " + thisID);
				Object[][] val = new Object[][] { { "init", "jmolvis" }, { "thisID", thisID + "*" },
						{ "token", Integer.valueOf(b ? T.on : T.off) }, { "thisID", null } };
				setJmolShape(JC.SHAPE_DRAW, val);
				setJmolShape(JC.SHAPE_ISOSURFACE, val);
			}
		} else if (shapes != null) {
			for (int i = 0; i < shapes.length; i++)
				shapes[i].visible = b;
		}
	}

	public static void removeAll(JmolWorldRenderer renderer, Group g) {
		// System.out.println("removeAll " + g.getName());
		Object[][] val = new Object[][] { { "init", "jmolvis" }, { "thisID", fixJ3dId(g.getName()) + "*" },
				{ "token", Integer.valueOf(T.off) }, { "thisID", null } };
		((Viewer) renderer.viewer).shm.setShapeProperties(JC.SHAPE_DRAW, val);
		((Viewer) renderer.viewer).shm.setShapeProperties(JC.SHAPE_ISOSURFACE, val);

	}

	public void setAppearance(Appearance app) {
		super.setAppearance(app);
		if (renderer != null)
			renderer.renderNode(this);
	}

	protected boolean getJmolVertices(JmolWorldRendererI renderer) {
		if (vertices == null)
			return false;
		Transform3D t = renderer.getTransform(this);
		if (t == null) {
			// disconnected
			return false;
		}
		if (jmolVertices == null)
			jmolVertices = new P3d[vertices.length];
		for (int i = vertices.length; --i >= 0;) {
			t.transform(vertices[i], ptemp);
			P3d p = jmolVertices[i];
			if (p == null)
				p = jmolVertices[i] = new P3d();
			p.set(ptemp.x, ptemp.y, ptemp.z);
		}
		return true;
	}

	protected String getJmolDrawApp(boolean andClose) {
		String s = "";
		String color = " color";
		float t = getTranslucency();
		if (t > 0) {
			s += " translucent " + t;
			color = "";
		}
		if (getColor(j3dColor))
			s += color + " [" + j3dColor.x + " " + j3dColor.y + " " + j3dColor.z + "]";
		return (s.length() == 0 ? "" : s + (andClose ? ";\n" : ""));
	}

	protected float getTranslucency() {
		Appearance app = this.getAppearance();
		TransparencyAttributes att = app.getTransparencyAttributes();
		return (att == null || att.getTransparencyMode() == TransparencyAttributes.NONE ? 0 : att.getTransparency());
	}

	protected boolean getColor(Color3f c) {
		Material m = this.getAppearance().getMaterial();
		if (m == null)
			return false;
		m.getAmbientColor(c);
		return true;
	}

	protected String getThisID() {
		if (thisID == null) {
			thisID = fixJ3dId(getName());
		} else if (shape == null && shapes == null) {
			getShapes();
		}
		return "draw id '" + thisID + "'";
	}

	protected void getShapes() {
		if (isosurfaceCount == 0) {
			shape = getDraw().getMesh(thisID);
		} else {
			Isosurface s = (Isosurface) getViewer().shm.getShape(JC.SHAPE_ISOSURFACE);
			shapes = new Mesh[isosurfaceCount];
			for (int i = 0; i < isosurfaceCount; i++) {
				String name = thisID + "_" + (i + 1);
				shapes[i] = s.getMesh(name);
			}
		}
	}

	protected Draw getDraw() {
		return (Draw) getViewer().shm.getShape(JC.SHAPE_DRAW);
	}


	protected double distance(int i, int j) {
		pt.sub2(jmolVertices[i], jmolVertices[j]);
		return pt.length();
	}

	protected String setJmolShape(int type, Object[][] val) {
		getViewer().shm.setShapeProperties(type, val);
		return "";
	}

	protected static T3d jmolPt(Tuple3d p3d, T3d p) {
		if (p == null)
			p = new P3d();
		p.x = p3d.x;
		p.y = p3d.y;
		p.z = p3d.z;
		return p;
	}

	protected void scriptShape(String cmd) {
		renderer.scriptWait(cmd);
		getShapes();
	}

	protected void draw(Object[][] val) {
		setJmolShape(JC.SHAPE_DRAW, val);
		getShapes();
	}

	protected String recalcVertices(Transform3D tr, short colix) {
		if (colix == Short.MIN_VALUE)
			colix = getJmolColor();
		M4d m = M4d.newA16(tr.mat);
		if (isosurfaceCount == 0) {
			if (shape == null) {
				System.out.println("JS3d ??? " + getName());
				return "";
			}
			setShapeVertices(shape, tr);
			if (colix != Short.MAX_VALUE)
				shape.colix = colix;
		} else {
			for (int i = 0; i < isosurfaceCount; i++) {
				// let Jmol calculate the alertative vertices and normals directly
				shapes[i].mat4 = m;
				shapes[i].recalcAltVertices = true;
				shapes[i].getOffsetVertices(null);
				shapes[i].colix = colix;
			}
		}
		return "";
	}

	protected static void setShapeVertices(Mesh shape, Transform3D tr) {
		shape.mat4 = M4d.newA16(tr.mat);
		shape.recalcAltVertices = true;
		shape.getOffsetVertices(null);
	}

	protected short getJmolColor() {
		getColor(j3dColor);
		argb = CU.colorTriadToFFRGB(j3dColor.x, j3dColor.y, j3dColor.z);
		translucency = getTranslucency();
		return C.getColixTranslucent3(C.getColix(argb), translucency > 0, translucency);
	}

	private static String fixJ3dId(String name) {
		return name.replace('*', '_').replace('\'', '_').replace(':', '.');
	}

	protected Viewer getViewer() {
		return (Viewer) renderer.getViewer();
	}

}
	//
//		System.out.println("JS dump " + getName());
//		for (int i = 0; i < vertices.length; i++) {
//			System.out.println(i + "\t" + vertices[i] + "\t" + jmolVertices[i]);
//		}
//		
//	}
