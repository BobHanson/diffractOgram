package j3djmol.geometry;

import javax.media.j3d.Appearance;

public class JmolArrow extends JmolShape3D {

	
	private double radiusArrow;
	private double lenArrow;
	private double radius;

	public JmolArrow(String name, double radiusArrow, double lenArrow, double radius, float height, int precision, Appearance app) {
		super(name, app);
		this.radiusArrow = radiusArrow;
		this.lenArrow = lenArrow;
		this.radius = radius;
		}
	
}