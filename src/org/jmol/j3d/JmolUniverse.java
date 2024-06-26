package org.jmol.j3d;

import javax.swing.JPanel;

import org.epfl.diffractogram.model3d.Univers;
import org.epfl.diffractogram.util.WorldRenderer;

public class JmolUniverse extends Univers {

	public JmolUniverse(JPanel panel3d) {
		super(panel3d);
	}
	
	protected WorldRenderer getRenderer(JPanel panel3d) {
		return new JmolWorldRenderer(panel3d, this);
	}
	
}
