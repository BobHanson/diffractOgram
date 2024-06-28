package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.jmol.j3d.JmolWorldRendererI;

import javajs.util.P3d;

public class JmolBox extends JmolShape3D {

	public JmolBox(String name, double dx, double dy, double dz, Appearance app) {
		super(name, app, JMOL_SHAPE_BOX);
		vertices = new Point3d[4];
		// o {1 0 0} {0 1 0} {0 0 1}
		vertices[0] = new Point3d(-dx, -dy, -dz);
		vertices[1] = new Point3d(dx, -dy, -dz);
		vertices[2] = new Point3d(-dx, dy, -dz);
		vertices[3] = new Point3d(-dx, -dy, dz);
	}

	Vector3d v3d = new Vector3d();

	@Override
	public String renderScript(JmolWorldRendererI renderer) {
		// use DRAW script with j3d coords and pass transform to Jmol
		Transform3D tr = renderer.getTransform(this);
		if (tr == null)
			return "";
		if (shape == null) {
			// initially use the draw UNITCELL option; otherwise it is too complicated
			P3d p = new P3d();
			String s = getThisID() + " unitcell [ " + jmolPt(vertices[0], p);
			v3d.sub(vertices[1], vertices[0]);
			s += jmolPt(v3d, p);
			v3d.sub(vertices[2], vertices[0]);
			s += jmolPt(v3d, p);
			v3d.sub(vertices[3], vertices[0]);
			s += jmolPt(v3d, p);
			s += " ]" + getJmolDrawApp(false) + " fill nomesh\n";
			scriptShape(s);
		}
		return recalcVertices(tr, Short.MIN_VALUE);
	}

	// notes for future reference
	// if (coords == null) {
//				coords = new P3d[] { o = new P3d(), a = new P3d(), b = new P3d(), c = new P3d() };
//				v = new Lst<Object>();
//			}
//			getJmolVertices(renderer);
//			o.setT(jmolVertices[0]);
//			b.sub2(jmolVertices[1], jmolVertices[0]);
//			a.sub2(jmolVertices[2], jmolVertices[0]);
//			c.sub2(jmolVertices[3], jmolVertices[0]);
//
//			P3d[] polygon = getViewer().getSymTemp().getUnitCell(coords, false, null).getCanonicalCopy(0, true);
//
//			getViewer().getTriangulator();
//			v.clear();
//			v.addLast(polygon);
//			v.addLast(Triangulator.fullCubePolygon);
//
//			getThisID();
//			Object[][] val = new Object[][] { { "init", "jmolcylinder" }, { "thisID", thisID },
//					{ "points", Integer.valueOf(0) }, { "polygon", v }, { "set", null },
//					{ "color", Integer.valueOf(argb) }, { "translucentLevel", Double.valueOf(t) },
//					{ "translucency", "translucent" }, { "token", Integer.valueOf(T.fill) },
//					{ "token", Integer.valueOf(T.nomesh) }, { "thisID", null } };
//			super.setJmolShape(JC.SHAPE_DRAW, val);
//	
//	
//	pt.sub2(jmolVertices[1], jmolVertices[0]);
//	String a = pt.toString();
//	pt.sub2(jmolVertices[2], jmolVertices[0]);
//	String b = pt.toString();
//	pt.sub2(jmolVertices[3], jmolVertices[0]);
//	String c = pt.toString();
//	String s = getThisID() + " unitcell [ " + jmolVertices[0] + a + b + c + " ]" + getJmolDrawApp(false)
//			+ " fill nomesh\n";
//	System.out.println(s);
//	return s;

}