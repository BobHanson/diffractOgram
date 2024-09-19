package org.epfl.diffractogram.model3d;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.epfl.diffractogram.DefaultValues;
import org.epfl.diffractogram.util.Calc;
import org.epfl.diffractogram.util.Colors;
import org.epfl.diffractogram.util.Utils3d;

public abstract class ProjScreen3d extends BranchGroup {
	
	public static final String FLAT = "Flat";
	public static final String CYLINDRICAL = "Cylindrical";

	protected Texture2D texture;
	protected int textureWidth, textureHeight;
	private ImageComponent2D ic2d;
	protected Appearance app;
	protected TransformGroup rotTg, transTg, resizeTg, lastTg, noSizeTg;
	public double y;
	public double w, h;
    protected Univers univers;

	public ProjScreen3d(Univers univers, Model3d.Precession precessionClass, String name) {
		setName(name);
		this.univers = univers;
		setCapability(BranchGroup.ALLOW_DETACH);
		setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

		rotTg = precessionClass.addPrecessionObject(univers.newWritableTransformGroup(null));
		transTg = univers.newWritableTransformGroup(null);
		resizeTg = univers.newWritableTransformGroup(null);
		rotTg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		rotTg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

		lastTg = resizeTg;
		noSizeTg = rotTg;
		rotTg.addChild(resizeTg);

		transTg.addChild(rotTg);
		addChild(transTg);

		app = app(new Color3f(.8f, .8f, .8f));
	}

	public static Appearance app(Color3f c) {
		Appearance app = Utils3d.newAppearance("color:" + c.toString());
		app.setMaterial(new Material(c, Colors.black, c, Colors.white, 128));
		// app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
		app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, .5f));
		PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		pa.setBackFaceNormalFlip(true);
		app.setPolygonAttributes(pa);
//		Transform3D t = new Transform3D();
//		t.set(new Matrix3d(1, 0, 0, 0, -1, 0, 0, 0, 1));
//		TextureAttributes ta = new TextureAttributes();
//		ta.setTextureTransform(t);
//		app.setTextureAttributes(ta);
		return app;
	}

	public abstract void setPos(double y);

	public abstract void setSize(double w, double h);

	public abstract boolean projPoint(Point3d v, Vector3d n, double d);

	public abstract Point.Double proj3dTo2d(Vector3d p);

	int cadreid;	

	static public class Cylindrical extends ProjScreen3d {
		private BranchGroup cadre, baseRayCyl;

		public Cylindrical(Univers univers, Model3d.Precession precession) {
			super(univers, precession, "screen:cyl:");
			Node c = univers.renderer.createCylinder("screen:cyl:", 1.0, 0.5, true, 100, 1, app);
			Transform3D t = new Transform3D();
			t.rotX(Math.PI / 2);
			TransformGroup tg = univers.newWritableTransformGroup(t);
			t.rotX(Math.PI);
			TransformGroup tginv = univers.newWritableTransformGroup(t);
			t.setIdentity();
			//t.set(new Vector3d(0,-0.5,0));
			TransformGroup tgtrans = univers.newWritableTransformGroup(t);
			tg.addChild(c);
			tginv.addChild(tg);
			tgtrans.addChild(tginv);
			lastTg.addChild(tgtrans);
			createLabel();
		}

		private void createCadre(double h, double y) {
			if (cadre != null)
				univers.removeNotify(noSizeTg, cadre);
			cadre = new BranchGroup();
			cadre.setCapability(BranchGroup.ALLOW_DETACH);
			cadre.setName("screen:cylf:");
			Transform3D t = new Transform3D();
			TransformGroup tgtor1 = Utils3d.getVectorTransformGroup(0, h / 4, 0, t);
			TransformGroup tgtor2 = Utils3d.getVectorTransformGroup(0, -h / 4, 0, t);
			t.rotX(Math.PI / 2);
			TransformGroup tgtor3 = univers.newWritableTransformGroup(t);
			++cadreid;
			Node t1 = univers.renderer.createTorus("screen:cylf:frame_1_" + cadreid ,.03, y, 10, 50, Colors.appBlack);
			Node t2 = univers.renderer.createTorus("screen:cylf:frame_2_" + cadreid,.03, y, 10, 50, Colors.appBlack);
			Utils3d.setParents(t1, tgtor1, tgtor3, cadre);
			Utils3d.setParents(t2, tgtor2, tgtor3, cadre, noSizeTg);
		}

		private void createBaseRay(double y) {
			if (DefaultValues.isUnitSphere)
				return;
			if (baseRayCyl != null)
				univers.removeNotify(this, baseRayCyl);
			baseRayCyl = univers.creator.createCylinder(univers, "baseray", new Point3d(), new Point3d(0, y, 0), .01,
					Colors.appOrange, 8);
			univers.addNotify(this, baseRayCyl);
		}

		private void createLabel() {
//			tg3 = univers.newTransformGroup(null);
//			tg3.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
//			addChild(tg3);
//			Appearance app2=new Appearance();
//			app2.setMaterial(new Material(black, black, black, white, 128));
//			Group l = univers.creator.createFixedLegend("Diffraction screen", new Point3d(0, y, 0), .1f, app2, false);
//			Transform3D tl = new Transform3D();
//			tl.set(new Vector3d(-0.8, -.1, -.5));
//			TransformGroup tgl = univers.newTransformGroup(tl);
//			tgl.addChild(l);
//			tg3.addChild(tgl);
		}

		public void setSize(double w, double h) {
			if (h == this.h && w == this.w)
				return;
			this.h = h;
			this.w = w;
			setPos(y);
		}

		public void setPos(double screenDistance) {
			this.y = screenDistance;
			Matrix3d m = new Matrix3d(
					screenDistance, 0, 0,
					0, screenDistance, 0, 
					0, 0, h);
			Transform3D t = new Transform3D();
			t.set(new Vector3d(0, DefaultValues.isUnitSphere ? -2 : DefaultValues.directRays ? -1.33333 : 0, -0)); // -2 for direct unit
			transTg.setTransform(t);
			t.set(m);
			resizeTg.setTransform(t);
			if (y == 0)
				return;
			createCadre(h, screenDistance);
			createBaseRay(screenDistance);
		}

		public boolean projPoint(Point3d v, Vector3d n, double d) {
			return Calc.projPointCylinder(v, n, d, y);
		}

		public Point.Double proj3dTo2d(Vector3d p) {
			return Calc.projTo2dCylinder(p, h);
		}
	}

	static public class Flat extends ProjScreen3d {
		protected QuadArray quad;
		private BranchGroup cadre1, cadre2, cadre3, cadre4, baseRayFlat;
		private TransformGroup tgLabel;
		private Transform3D t3dLabel;

		public Flat(Univers univers, Model3d.Precession precession) {
			super(univers, precession, "screen:flat:");
			lastTg.addChild(univers.renderer.createQuad("screen:flat:", Utils3d.createQuad(), app));
		}

		private void createLabel(double w, double h) {
			if (tgLabel == null) {
				t3dLabel = new Transform3D();
				tgLabel = univers.newWritableTransformGroup(null);
				Appearance appLabel = new Appearance();
				appLabel.setMaterial(new Material(Colors.black, Colors.black, Colors.black, Colors.white, 128));
				Node l = univers.creator.createFixedLegend("ds", "Diffraction screen", new Point3d(0, y, 0), .2f, appLabel, true);
				l.setName("screen:flat:legend:diffscreen");
				Utils3d.setParents(l, tgLabel, noSizeTg);
			}
			Vector3d v = new Vector3d(0, 0, 4.2 * h / w);
			t3dLabel.set(v, w / 8);
			tgLabel.setTransform(t3dLabel);
		}

		private void createCadre(double w, double h) {
			if (cadre1 == null) {
				cadreid++;
				noSizeTg.addChild(cadre1 = univers.creator.createCylinder(univers, "screen:flat:frame1_" + cadreid, new Point3d(-w / 2, 0, -h / 2),
						new Point3d(-w / 2, 0, h / 2), .03, Colors.appBlack, 10));
				noSizeTg.addChild(cadre2 = univers.creator.createCylinder(univers, "screen:flat:frame2_" + cadreid, new Point3d(w / 2, 0, -h / 2),
						new Point3d(w / 2, 0, h / 2), .03, Colors.appBlack, 10));
				noSizeTg.addChild(cadre3 = univers.creator.createCylinder(univers, "screen:flat:frame3_" + cadreid, new Point3d(-w / 2, 0, h / 2),
						new Point3d(w / 2, 0, h / 2), .03, Colors.appBlack, 10));
				noSizeTg.addChild(cadre4 = univers.creator.createCylinder(univers, "screen:flat:frame4_" + cadreid, new Point3d(w / 2, 0, -h / 2),
						new Point3d(-w / 2, 0, -h / 2), .03, Colors.appBlack, 10));
			} else {
				Utils3d.changeCylinder(cadre1, new Point3d(-w / 2, 0, -h / 2), new Point3d(-w / 2, 0, h / 2));
				Utils3d.changeCylinder(cadre2, new Point3d(w / 2, 0, -h / 2), new Point3d(w / 2, 0, h / 2));
				Utils3d.changeCylinder(cadre3, new Point3d(-w / 2, 0, h / 2), new Point3d(w / 2, 0, h / 2));
				Utils3d.changeCylinder(cadre4, new Point3d(w / 2, 0, -h / 2), new Point3d(-w / 2, 0, -h / 2));
			}
		}

		private void createBaseRay(double y) {
			if (DefaultValues.isUnitSphere)
				return;
			if (baseRayFlat != null)
				univers.removeNotify(this, baseRayFlat);
			baseRayFlat = univers.creator.createCylinder(univers, "baserayflat", new Point3d(), new Point3d(0, y, 0), .02,
					Colors.appOrange, 8);
			univers.addNotify(this, baseRayFlat);
		}

		public void setSize(double w, double h) {
			if (this.w == w && this.h == h)
				return;
			this.w = w;
			this.h = h;
			Matrix3d m = new Matrix3d(
					w, 0, 0, 
					0, 1, 0, 
					0, 0, h);
			Transform3D t = new Transform3D();
			t.set(m);
			resizeTg.setTransform(t);
			createCadre(w, h);
			createLabel(w, h);
		}

		public void setPos(double screenDistance) {
			y = screenDistance;
			Transform3D t = new Transform3D();
			t.set(new Vector3d(0, screenDistance, 0));
			transTg.setTransform(t);
			createBaseRay(screenDistance);
		}

		public boolean projPoint(Point3d v, Vector3d n, double d) {
			return Calc.projPointFlat(v, n, d);
		}

		public Point.Double proj3dTo2d(Vector3d p) {
			return Calc.projTo2dFlat(p, w, h);
		}
	}

	public void setImage(Image ingIn) {
		if (texture == null || textureWidth != ingIn.getWidth(null) || textureHeight != ingIn.getHeight(null)) {
			textureWidth = ingIn.getWidth(null);
			textureHeight = ingIn.getHeight(null);
			texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, textureWidth, textureHeight);
			ic2d = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, (BufferedImage) ingIn, false, false);
			ic2d.setCapability(ImageComponent2D.ALLOW_IMAGE_WRITE);
			texture.setImage(0, ic2d);
			texture.setCapability(Texture2D.ALLOW_IMAGE_READ);
			texture.setEnable(true);
			texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
			texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
			app.setTexture(texture);
		} else {
			ic2d.set((BufferedImage) ingIn);
		}
	}
}
