package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;
import javax.vecmath.Point3d;

import org.jmol.j3d.JmolWorldRenderer;

public class JmolCylinder extends JmolShape3D {

	private double radius;
	private double height;
	private boolean isHollow; // not yet implemented

	public JmolCylinder(String name, double radius, double height, boolean isHollow, int xdiv, int ydiv, Appearance app) {
		super(name, app, JMOL_SHAPE_CYLINDER);
		this.radius = radius;
		this.height = height;
		this.isHollow = isHollow;
		vertices = new Point3d[4];
		vertices[0] = new Point3d(0, -height/2, 0);
		vertices[1] = new Point3d(0, height/2, 0);
		vertices[2] = new Point3d(0, 0, 0);
		vertices[3] = new Point3d(radius, 0, 0);
	}

	@Override
	public String renderScript(JmolWorldRenderer renderer) {
		if (!getJmolVertices(renderer))
			return "";
		pt.sub2(jmolVertices[3], jmolVertices[2]);
		double r = pt.length();
		return getDrawId() 
			+ " width " + r*2 + " " + jmolVertices[0] + jmolVertices[1]
			+ getJmolDrawApp(true);
	}
	
	
}