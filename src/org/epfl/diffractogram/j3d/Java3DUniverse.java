package org.epfl.diffractogram.j3d;

import javax.swing.JPanel;

import org.epfl.diffractogram.model3d.Univers;
import org.epfl.diffractogram.model3d.WorldRenderer;

public class Java3DUniverse extends Univers {

	public Java3DUniverse(JPanel panel3d) {
		super(panel3d);
	}
	
	protected WorldRenderer getRenderer(JPanel panel3d) {
		return new Java3DWorldRenderer(panel3d, this);
	}
	
}
