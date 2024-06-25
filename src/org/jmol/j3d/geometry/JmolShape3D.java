package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import org.epfl.diffractogram.util.WorldRenderer;
import org.jmol.j3d.JmolWorldRenderer;

import javajs.util.P3d;

public abstract class JmolShape3D extends Shape3D {

	public final static int JMOL_SHAPE_SPHERE = 1;
	public final static int JMOL_SHAPE_ATOM = 2;
	public final static int JMOL_SHAPE_CYLINDER = 3;
	public final static int JMOL_SHAPE_ARROW = 4;
	public final static int JMOL_SHAPE_BOX = 5;
	public final static int JMOL_SHAPE_TORUS = 6;
	public final static int JMOL_SHAPE_TEXT = 7;
	public final static int JMOL_SHAPE_PANEL = 8;

	Point3d[] vertices;
	P3d[] jmolVertices;

	int type;

	JmolShape3D(String name, Appearance app, int type) {
		this.type = type;
		setCapability(ALLOW_APPEARANCE_WRITE);
		setName(name);
		setAppearance(app);
		if (!WorldRenderer.completed)
			JmolWorldRenderer.allObjects.add(this);

	}

	public void setAppearance(Appearance app) {
		super.setAppearance(app);
		if (getName() != null && app != null)
			System.out.println("JS app set " + getName() + " " + app.getName());
	}

	public abstract String renderScript(JmolWorldRenderer renderer);

	protected final static Transform3D t3d = new Transform3D();
	protected final static Transform3D t = new Transform3D();

	public Transform3D getTransform(JmolWorldRenderer renderer, Transform3D ret) {
		if (ret == null)
			ret = t3d;
		ret.setIdentity();
		Node n = this;
		while ((n = n.getParent()) != null) {
			if (n == renderer.root) {
				ret.mul(renderer.topTransform, ret);
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

	final static Point3d ptemp = new Point3d();

	public boolean getJmolVertices(JmolWorldRenderer renderer) {
		return transformVertices(getTransform(renderer, null));
	}

	public boolean transformVertices(Transform3D t) {
		if (vertices == null || t == null)
			return false;
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

	protected final static P3d pt = new P3d();

	protected final static Color3f c = new Color3f();
	
	public String getJmolDrawApp(boolean andClose) {
		String s = "";
		String color = " color";
		float t = getTranslucency();
		if (t > 0) {
			s += " translucent " + t;
			color = "";
		}
		if (getColor(c)) 
			s += color + " [" + c.x  + " " + c.y + " " + c.z + "]";
		return (s.length() == 0 ? "" : s + (andClose ? ";\n" : ""));
	}

	private float getTranslucency() {
       Appearance app = this.getAppearance();
       TransparencyAttributes att = app.getTransparencyAttributes();
       return (att == null  || att.getTransparencyMode() == TransparencyAttributes.NONE ? 0 : att.getTransparency());
	}

	private boolean getColor(Color3f c) {
		Material m = this.getAppearance().getMaterial();
		if (m == null)
			return false;
		m.getAmbientColor(c);
		return true;
	}

	public String getDrawId() {
		return "draw id '" + getName().replace('*', '_').replace('\'','_') + "'";
	}

	//
//		System.out.println("JS dump " + getName());
//		for (int i = 0; i < vertices.length; i++) {
//			System.out.println(i + "\t" + vertices[i] + "\t" + jmolVertices[i]);
//		}
//		
//	}
}