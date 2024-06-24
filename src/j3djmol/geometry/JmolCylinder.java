package j3djmol.geometry;

import javax.media.j3d.Appearance;

public class JmolCylinder extends JmolShape3D {

	private double radius;
	private double height;
	private boolean isHollow;

	public JmolCylinder(String name, double radius, double height, boolean isHollow, int xdiv, int ydiv, Appearance app) {
		super(name, app);
		this.radius = radius;
		this.height = height;
		this.isHollow = isHollow;
	}
	
}