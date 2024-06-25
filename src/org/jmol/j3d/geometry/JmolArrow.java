package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;
import javax.vecmath.Point3d;

import org.jmol.j3d.JmolWorldRenderer;

public class JmolArrow extends JmolShape3D {

	
	private double radiusArrow;
	private double lenArrow;
	private double radius;

	public JmolArrow(String name, double radiusArrow, double lenArrow, double radius, float height, int precision,
			Appearance app) {
		super(name, app, JMOL_SHAPE_ARROW);
		this.radiusArrow = radiusArrow;
		this.lenArrow = lenArrow;
		this.radius = radius;

		vertices = new Point3d[4];
		vertices[0] = new Point3d(0, -height / 2, 0);
		vertices[1] = new Point3d(0, height / 2, 0);
		vertices[2] = new Point3d(0, 0, 0);
		vertices[3] = new Point3d(radius, 0, 0);
	}
	
	@Override
	public String renderScript(JmolWorldRenderer renderer) {
		if (!getJmolVertices(renderer))
			return "";
		
		pt.sub2(jmolVertices[3], jmolVertices[2]);
		double r = pt.length();
		pt.sub2(jmolVertices[1], jmolVertices[0]);
		
		String s = getDrawId() 
				+ " width " + r*2 + " vector " + jmolVertices[0] + pt 	
				+ getJmolDrawApp(true);
		return s;
	}
	

}