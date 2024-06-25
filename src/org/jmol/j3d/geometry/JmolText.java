package org.jmol.j3d.geometry;

import java.awt.Font;

import javax.media.j3d.BranchGroup;

import org.jmol.j3d.JmolWorldRenderer;

public class JmolText extends JmolShape3D {
	String text;
	int align;
	int path;
	Font font;

	public JmolText(String name, BranchGroup bg, String text, Font font, int align, int path) {
		super(name, null, JMOL_SHAPE_TEXT);
	   this.text = text;
	   this.font = font;
	   this.align = align;
	   this.path = path;
	}

	@Override
	public String renderScript(JmolWorldRenderer renderer) {
		// TODO Auto-generated method stub
		return "";
	}
	

}