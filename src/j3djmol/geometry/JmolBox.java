package j3djmol.geometry;

import javax.media.j3d.Appearance;
import javax.media.j3d.Shape3D;

public class JmolBox extends JmolShape3D {

	private double dx, dy, dz;

	public JmolBox(String name, double dx, double dy, double dz, Appearance app) {
		super(name, app);
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}
	
}