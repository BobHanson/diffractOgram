package j3djmol.geometry;

import javax.media.j3d.Appearance;
import javax.media.j3d.Shape3D;

public class JmolShape3D extends Shape3D {

	JmolShape3D(String name, Appearance app) {
		setCapability(ALLOW_APPEARANCE_WRITE);
		setName(name);
		setAppearance(app);
	}

	public void setAppearance(Appearance app) {
		super.setAppearance(app);
		if (getName() != null && app != null)
			System.out.println("JS app set " + getName() + " " + app.getName());
	}			

}