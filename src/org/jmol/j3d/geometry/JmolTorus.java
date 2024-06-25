package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;

import org.jmol.j3d.JmolWorldRenderer;

public class JmolTorus extends JmolShape3D {

	private double innerRadius;
	private double outerRadius;
	private int innerFaces;
	private int outerFaces;

	public JmolTorus(String name, double innerRadius, double outerRadius, int innerFaces, int outerFaces, Appearance app) {
		super(name, app, JMOL_SHAPE_TORUS);
		this.innerRadius = innerRadius;
		this.outerRadius = outerRadius;
		this.innerFaces = innerFaces;
		this.outerFaces = outerFaces;
	}
	
	@Override
	public String renderScript(JmolWorldRenderer renderer) {
		// TODO Auto-generated method stub
		return "";
	}
	
}