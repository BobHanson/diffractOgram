package org.epfl.diffractogram.util;

import java.awt.Font;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.OrientedShape3D;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Text3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import org.epfl.diffractogram.model3d.Univers;

public class JSWorldRenderer extends WorldRenderer {

	public JSWorldRenderer(JPanel panel3d, Univers univers) {
		super(panel3d, univers);
	}

	@Override
	public void setEnvironment(TransformGroup reset, BranchGroup root) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isParallel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setParallel(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBackgroundColor(Color3f bgColor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	public static Shape3D getTextShape(String s, Font font, int align, int path, Point3d rot,
			Appearance app) {
		Shape3D textShape;
		if (rot == null) {
			textShape = new Shape3D();
		} else {
			OrientedShape3D t = new OrientedShape3D();
			t.setAlignmentMode(OrientedShape3D.ROTATE_ABOUT_POINT);
			t.setRotationPoint(new Point3f(rot));
			textShape = t;
		}
		Font3D f3d = (Font3D) (Object) new JSFont3D(font, new FontExtrusion());
		Text3D txt = new Text3D(f3d, s, new Point3f(), align, path);
		textShape.setGeometry(txt);
		textShape.setAppearance(app);	
		return textShape;
	}

}
