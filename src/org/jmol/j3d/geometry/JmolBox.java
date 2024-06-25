package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;
import javax.vecmath.Point3d;

import org.jmol.j3d.JmolWorldRenderer;

public class JmolBox extends JmolShape3D {

	private double dx, dy, dz;

	public JmolBox(String name, double dx, double dy, double dz, Appearance app) {
		super(name, app, JMOL_SHAPE_BOX);
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		vertices = new Point3d[4];
		// o a b c
		vertices[0] = new Point3d(-dx, -dy, -dz);
		vertices[1] = new Point3d(dx, -dy, -dz);
		vertices[2] = new Point3d(-dx, dy, -dz);
		vertices[3] = new Point3d(-dx, -dy, dz);		
	}

	@Override
	public String renderScript(JmolWorldRenderer renderer) {
		if (!getJmolVertices(renderer))
			return "";
		pt.sub2(jmolVertices[1], jmolVertices[0]);
		String a = pt.toString();
		pt.sub2(jmolVertices[2], jmolVertices[0]);
		String b = pt.toString();
		pt.sub2(jmolVertices[3], jmolVertices[0]);
		String c = pt.toString();
		String s = getDrawId()
			+ " unitcell [ " + jmolVertices[0] + a + b + c + " ]" 
			+ getJmolDrawApp(false) + " fill nomesh\n";
		System.out.println(s);
		return s;
	}
	
}