package j3djmol.geometry;

import javax.media.j3d.Appearance;

public class JmolTorus extends JmolShape3D {

	private double innerRadius;
	private double outerRadius;
	private int innerFaces;
	private int outerFaces;

	public JmolTorus(String name, double innerRadius, double outerRadius, int innerFaces, int outerFaces, Appearance app) {
		super(name, app);
		this.innerRadius = innerRadius;
		this.outerRadius = outerRadius;
		this.innerFaces = innerFaces;
		this.outerFaces = outerFaces;
	}
	
}