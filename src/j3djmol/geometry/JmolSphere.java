package j3djmol.geometry;

import javax.media.j3d.Appearance;

public class JmolSphere extends JmolShape3D {

	private double radius;

	public JmolSphere(String name, double radius, int divs, boolean isAtom, Appearance app) {
		super(name, app);
		this.radius = radius;
	}
	
}