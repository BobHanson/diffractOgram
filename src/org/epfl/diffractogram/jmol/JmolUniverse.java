package org.epfl.diffractogram.jmol;

import javax.swing.JPanel;

import org.epfl.diffractogram.model3d.Univers;
import org.epfl.diffractogram.model3d.WorldRenderer;

public class JmolUniverse extends Univers {

	private JmolWorldRenderer jmolRenderer;

	public JmolUniverse(JPanel panel3d) {
		super(panel3d);
		allowArrowText = true;
	}

	protected WorldRenderer getRenderer(JPanel panel3d) {
		return jmolRenderer = new JmolWorldRenderer(panel3d, this);
	}

	@Override
	public void echo(String msg) {
		jmolRenderer.echo(msg);
	}

}
