package org.epfl.diffractogram.j3d;

import javax.media.j3d.BranchGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.epfl.diffractogram.model3d.Univers;
import org.epfl.diffractogram.model3d.WorldRenderer;
import org.epfl.diffractogram.util.Lattice;

public class Java3DUniverse extends Univers {

	public Java3DUniverse(JPanel panel3d) {
		super(panel3d);
		allowArrowText = false;
	}
	
	protected WorldRenderer getRenderer(JPanel panel3d) {
		return new Java3DWorldRenderer(panel3d, this);
	}

	@Override
	public void echo(String string) {
	    // n/a
	}

}
