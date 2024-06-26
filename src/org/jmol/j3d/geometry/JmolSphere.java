package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;
import javax.vecmath.Point3d;

import org.jmol.j3d.WorldRendererI;

public class JmolSphere extends JmolShape3D {

	private double radius;
	private int atomIndex;
	

	public JmolSphere(String name, double radius, int divs, boolean isAtom, Appearance app) {
		super(name, app, isAtom ? JMOL_SHAPE_ATOM : JMOL_SHAPE_SPHERE);
		this.radius = radius;
		vertices = new Point3d[2];
		vertices[0] = new Point3d(0, 0, 0);
		vertices[1] = new Point3d(radius, 0, 0);
	}

	@Override
	public String renderScript(WorldRendererI renderer) {
		if (!getJmolVertices(renderer))
			return "";
		pt.sub2(jmolVertices[0], jmolVertices[1]);
		double r = pt.length();
		
		
		String s = getDrawId() 
				+ " width " + r*2 + " " + jmolVertices[0] 
				+ getJmolDrawApp(true);
		return s;
	}
	
}