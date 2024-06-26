package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;
import javax.media.j3d.QuadArray;
import javax.vecmath.Point3d;

import org.jmol.j3d.WorldRendererI;

public class JmolQuad extends JmolShape3D {

	public JmolQuad(String name, QuadArray quad, Appearance app) {
		super(name, app, JMOL_SHAPE_PANEL);
		vertices = new Point3d[4];
		for (int i = 0; i < 4; i++) {
			quad.getCoordinate(i, vertices[i] = new Point3d());
		}
	}

	@Override
	public String renderScript(WorldRendererI renderer) {
		if (!getJmolVertices(renderer))
			return "";
		String s = getDrawId()
			+ jmolVertices[0]+ jmolVertices[1]+ jmolVertices[2]+ jmolVertices[3] + getJmolDrawApp(true);
		System.out.println(s);
		return s;
	}
	
}