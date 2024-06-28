package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;

import org.jmol.j3d.JmolWorldRendererI;

public class JmolQuad extends JmolShape3D {

	public JmolQuad(String name, QuadArray quad, Appearance app) {
		super(name, app, JMOL_SHAPE_PANEL);
		vertices = new Point3d[4];
		for (int i = 0; i < 4; i++) {
			quad.getCoordinate(i, vertices[i] = new Point3d());
		}
	}

	@Override
	public String renderScript(JmolWorldRendererI renderer) {
		// use Java3D vertices and pass transform to Jmol
		Transform3D tr = renderer.getTransform(this);
		if (tr == null)
			return "";
		short colix = getJmolColor();
		if (shape == null) {
			getThisID();
		    draw(new Object[][] { 
	           { "init", "jmolquad" },
	           { "thisID", thisID },
	           { "points", Integer.valueOf(0) },
	           { "coord", jmolPt(vertices[0], null) },
	           { "coord", jmolPt(vertices[1], null) },
	           { "coord", jmolPt(vertices[2], null) },
	           { "coord", jmolPt(vertices[3], null) },
	           { "set", null },
	           { "color", Integer.valueOf(argb) },
	           { "translucentLevel", Double.valueOf(translucency) },
	           { "translucency", "translucent" },
	           { "thisID", null }
	        });
		}
		return recalcVertices(tr, colix);
	}

	
}