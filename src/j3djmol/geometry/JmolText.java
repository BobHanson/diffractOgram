package j3djmol.geometry;

import java.awt.Font;

import javax.media.j3d.BranchGroup;

public class JmolText extends JmolShape3D {
	String text;
	int align;
	int path;
	Font font;

	public JmolText(String name, BranchGroup bg, String text, Font font, int align, int path) {
		super(name, null);
	   this.text = text;
	   this.font = font;
	   this.align = align;
	   this.path = path;
	}
	

}