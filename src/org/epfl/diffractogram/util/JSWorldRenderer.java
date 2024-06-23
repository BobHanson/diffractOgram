package org.epfl.diffractogram.util;

import java.awt.Font;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.epfl.diffractogram.model3d.Univers;
import org.epfl.diffractogram.model3d.Utils3d;

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

	private static class JSText3D extends Shape3D {
		String text;
		int align;
		int path;
		Font font;
		private float size;
		private BranchGroup bg;
		
		public JSText3D(BranchGroup bg, String text, Point3d pos, float size, Font font, int align, int path) {
		   this.bg = bg;
		   this.text = text;
		   this.font = font;
		   this.size = size;
		   this.align = align;
		   this.path = path;
		}
		
	}
	
	public static BranchGroup getTextShapeImpl(BranchGroup bg, String s, Point3d pos, float size, Font font, int align, int path, Point3d rotPoint,
			Appearance app) {
		Shape3D textShape = new JSText3D(bg, s, pos, size, font, align, path);

		Transform3D tsize = new Transform3D();
		tsize.set(size, new Vector3d(pos));
		
		Utils3d.setParents(textShape, new TransformGroup(tsize), bg);
		
		return bg;
	}

	public static Node createTorusImpl(double innerRadius, double outerRadius, int innerFaces, int outerFaces,
			Appearance app) {
		// TODO
		return newShape3D();
	}

	public static Node createBoxImpl(double dx, double dy, double dz, Appearance app) {
		// TODO
		return newShape3D();
	}

	public static Node createCylinderImpl(double radius, double height, boolean isHollow, int xdiv, int ydiv, Appearance app) {
		// TODO Auto-generated method stub
		return newShape3D();
	}

	private static int id = 0;
	public static Node createSphereImpl(double radius, int divs, boolean isAtom, Appearance app) {
		// TODO Auto-generated method stub
		Shape3D a = newShape3D();
		if (isAtom) {
			a.setName("atom " + ++id);
		}
		return a;
	}

	private static class JSShape3D extends Shape3D {
		
		JSShape3D() {
			setCapability(ALLOW_APPEARANCE_WRITE);
		}

		public void setAppearance(Appearance app) {
			super.setAppearance(app);
			if (getName() != null)
				System.out.println("JS" + getName() + " " + app.getName());
		}			
		
	}
	private static Shape3D newShape3D() {
		return new JSShape3D();
	}

	public static TransformGroup createArrowImpl(TransformGroup tg, double radiusArrow, double lenArrow, double radius,
			float height, int precision, Appearance app) {
		// TODO Auto-generated method stub
		tg.addChild(newShape3D());
		return tg;
	}

}
