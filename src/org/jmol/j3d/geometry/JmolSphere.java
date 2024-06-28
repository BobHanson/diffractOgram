package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;
import javax.vecmath.Point3d;

import org.jmol.j3d.JmolWorldRendererI;

public class JmolSphere extends JmolShape3D {


	public JmolSphere(String name, double radius, int divs, boolean isAtom, Appearance app) {
		super(name, app, isAtom ? JMOL_SHAPE_ATOM : JMOL_SHAPE_SPHERE);
		vertices = new Point3d[2];
		vertices[0] = new Point3d(0, 0, 0);
		vertices[1] = new Point3d(radius, 0, 0);
	}

	@Override
	public String renderScript(JmolWorldRendererI renderer) {
		if (!getJmolVertices(renderer))
			return "";
		double r = distance(0, 1);
		short colix = getJmolColor();
		if (shape == null) {
			getThisID();
			draw(new Object[][] { 
	           { "init", "jmolsphere" },
	           { "thisID", thisID },
	           { "width", Double.valueOf(r * 2) },
	           { "points", Integer.valueOf(0) },
	           { "coord", jmolVertices[0] },
	           { "set", null },
	           { "color", Integer.valueOf(argb) },
	           { "translucentLevel", Double.valueOf(translucency) },
	           { "translucency", "translucent" },
	           { "thisID", null }
	        });
			return "";
		}
		shape.vs[0].setT(jmolVertices[0]);
		shape.width = r * 2;
		shape.colix = colix;			
		return "";

	}
	
//	String s = getThisID() 
//	+ " width " + r*2 + " "
//	+ getJmolDrawApp(true);
//return s;
}