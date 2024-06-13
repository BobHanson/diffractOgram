package org.epfl.diffractogram.model3d;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;

import org.epfl.diffractogram.transformations.PrecessionClass;
import org.epfl.diffractogram.util.Java3dUtil.Appearance;
import org.epfl.diffractogram.util.Java3dUtil.BranchGroup;
import org.epfl.diffractogram.util.Java3dUtil.Color3f;
import org.epfl.diffractogram.util.Java3dUtil.Cylinder;
import org.epfl.diffractogram.util.Java3dUtil.ImageComponent2D;
import org.epfl.diffractogram.util.Java3dUtil.Material;
import org.epfl.diffractogram.util.Java3dUtil.Matrix3d;
import org.epfl.diffractogram.util.Java3dUtil.Point3d;
import org.epfl.diffractogram.util.Java3dUtil.PolygonAttributes;
import org.epfl.diffractogram.util.Java3dUtil.QuadArray;
import org.epfl.diffractogram.util.Java3dUtil.Shape3D;
import org.epfl.diffractogram.util.Java3dUtil.TexCoord2f;
import org.epfl.diffractogram.util.Java3dUtil.Texture;
import org.epfl.diffractogram.util.Java3dUtil.Texture2D;
import org.epfl.diffractogram.util.Java3dUtil.Transform3D;
import org.epfl.diffractogram.util.Java3dUtil.TransformGroup;
import org.epfl.diffractogram.util.Java3dUtil.TransparencyAttributes;
import org.epfl.diffractogram.util.Java3dUtil.Vector3d;
import org.epfl.diffractogram.util.Java3dUtil.Vector3f;
import org.epfl.diffractogram.util.Java3dUtil.Torus;

public abstract class ProjScreen3d extends BranchGroup implements ColorConstants {
	protected Texture2D texture;
	protected int textureWidth, textureHeight;
	private ImageComponent2D ic2d;
	protected Appearance app;
	protected TransformGroup rotTg, transTg, resizeTg, lastTg, noSizeTg;
	public double y;
	public double w, h; 
	public Vector3d OyO;
	
	public ProjScreen3d(PrecessionClass precessionClass) {
		setCapability(BranchGroup.ALLOW_DETACH);
		setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		
		rotTg = precessionClass.new PrecessionObject();
		transTg = new TransformGroup();
		resizeTg = new TransformGroup();
		transTg.setCapabilityTo(TransformGroup.ALLOW_TRANSFORM_WRITE);
		resizeTg.setCapabilityTo(TransformGroup.ALLOW_TRANSFORM_WRITE);
		rotTg.setCapabilityTo(BranchGroup.ALLOW_CHILDREN_EXTEND);
		rotTg.setCapabilityTo(BranchGroup.ALLOW_CHILDREN_WRITE);
		
		lastTg = resizeTg;
		noSizeTg = rotTg;
		rotTg.addChild(resizeTg);
		
		transTg.addChild(rotTg);
		addChild(transTg);
		
		app = app(new Color3f(.8f, .8f, .8f));
	}
	
	public static Appearance app(Color3f c) {
		Appearance app = new Appearance();
		app.setMaterial(new Material(c, black, c, white, 128));
	  //app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
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
	
	static public class Cylindric extends ProjScreen3d {
		private BranchGroup cadre, baseRay;
		public Cylindric(PrecessionClass precessionClass) {
			super(precessionClass);
			Cylinder c = new Cylinder(1f, .5f, Cylinder.GENERATE_NORMALS|Cylinder.GENERATE_TEXTURE_COORDS, 100, 1, app);
			c.removeChild(c.getShape(Cylinder.BOTTOM));
			c.removeChild(c.getShape(Cylinder.TOP));
			Transform3D t = new Transform3D();
			t.rotX(Math.PI/2);
			TransformGroup tg = new TransformGroup(t);
			t.rotX(Math.PI);
			TransformGroup tginv = new TransformGroup(t);
			tg.addChild(c);
			tginv.addChild(tg);
			lastTg.addChild(tginv);
			createLabel();
		}
		private void createCadre(double h, double y) {
			if (cadre!=null) noSizeTg.removeChild(cadre);
			cadre = new BranchGroup();
			cadre.setCapability(BranchGroup.ALLOW_DETACH);
			Torus tor1 = new Torus(.03f, (float)y, 10, 50, Utils3d.createApp(black));
			Torus tor2 = new Torus(.03f, (float)y, 10, 50, Utils3d.createApp(black));
			Transform3D t = new Transform3D();
			t.set(new Vector3d(0, h/4, 0));
			TransformGroup tgtor1 = new TransformGroup(t);
			t.set(new Vector3d(0, -h/4, 0));
			TransformGroup tgtor2 = new TransformGroup(t);
			t.rotX(Math.PI/2);
			TransformGroup tgtor3 = new TransformGroup(t);
			tgtor1.addChild(tor1);
			tgtor2.addChild(tor2);
			tgtor3.addChild(tgtor1);
			tgtor3.addChild(tgtor2);
			cadre.addChild(tgtor3);
			noSizeTg.addChild(cadre);
		}
		private void createBaseRay(double y) {
			if (baseRay!=null) removeChild(baseRay);
			baseRay = Utils3d.createCylinder(new Point3d(), new Point3d(0, y, 0), .01, Utils3d.createApp(ColorConstants.yellow), 8);
			addChild(baseRay);
		}
		private void createLabel() {
//			tg3 = new TransformGroup();
//			tg3.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
//			addChild(tg3);
//			Appearance app2=new Appearance();
//			app2.setMaterial(new Material(black, black, black, white, 128));
//			Group l = Utils3d.createFixedLegend("Diffraction screen", new Point3d(0, y, 0), .1f, app2, false);
//			Transform3D tl = new Transform3D();
//			tl.set(new Vector3d(-0.8, -.1, -.5));
//			TransformGroup tgl = new TransformGroup(tl);
//			tgl.addChild(l);
//			tg3.addChild(tgl);
		}
		
		public void setSize(double w, double h) {
			this.h=h; this.w=w;
			Matrix3d m = new Matrix3d(y, 0, 0, 0, y, 0, 0, 0, h); 
			Transform3D t = new Transform3D();
			transTg.setTransform(t);
			t.set(m);
			resizeTg.setTransform(t);
			createCadre(h, y);
		}
		public void setPos(double y) {
			this.y=y;
			OyO = new Vector3d(0, y, 0);
			Matrix3d m = new Matrix3d(y, 0, 0, 0, y, 0, 0, 0, h); 
			Transform3D t = new Transform3D();
			transTg.setTransform(t);
			t.set(m);
			resizeTg.setTransform(t);
			createCadre(h, y);
			createBaseRay(y);
		}

		
		
		
		
		
		
		
		
/*
		public Point.Double proj3dTo2d(Vector3d p) {
			if (p.x>w/2||-p.x>w/2||p.y>h/2||-p.y>h/2) return null;
			return new Point.Double(p.x/w, -p.z/h);
		}
*/		

		
		
//		public Point3d getProjPoint(Point3d a, Point3d b, Vector3d n) {
//			Vector3d v = new Vector3d();
//			v.sub(b, a);
//			double t = Math.sqrt((y*y)/((v.x*v.x)+(v.y*v.y)));
//			if (t<0) return null;
//			v.scale(t);
//			return new Point3d(v);
//		}
		
		
		//private Vector3d vOriented = new Vector3d();
		//private double t;
		public boolean projPoint(Point3d v, Vector3d n, double d) {
			//vOriented.set(v);
			//Orientation.apply(vOriented);
			//t = Math.sqrt((y*y)/((vOriented.x*vOriented.x)+(vOriented.y*vOriented.y)));
			double t = Math.sqrt((y*y)/((v.x*v.x)+(v.y*v.y)));
			if (t<0) return false;
			v.scale(t);
			return true;
		}
		
		
		
//		public Point.Double proj3dTo2d(Point3d p) {
//			double d = Math.atan(p.x/p.y)/Math.PI/2.0;
//			return new Point.Double(d+(p.y<0?(p.x<0?-.5:.5):0), -p.z/h*2);
//		}
//		public boolean isInside(Point3d p) {
//			return p.z>=-h/4 && p.z<=h/4;
//		}
		
		
		public Point.Double proj3dTo2d(Vector3d p) {
			if (p.z<-h/4 || p.z>h/4) return null;
			//vOriented.scale(t);
//			double d = Math.atan(p.y/p.x)/Math.PI;
//			return new Point.Double(-d, -p.z/h*2);
			double d = Math.atan(p.x/p.y)/Math.PI/2.0;
			return new Point.Double(d+(p.y<0?(p.x<0?-.5:.5):0), -p.z/h*2);
		}
	}
	
	static public class Flat extends ProjScreen3d {
		protected QuadArray quad;
		private BranchGroup cadre1, cadre2, cadre3, cadre4, baseRay;
		private TransformGroup tgLabel;
		private Transform3D t3dLabel;
		
		public Flat(PrecessionClass precessionClass) {
			super(precessionClass);
			lastTg.addChild(new Shape3D(createQuad(), app));
		}
		
		public static QuadArray createQuad() {
			QuadArray quad = new QuadArray(4, QuadArray.COORDINATES|QuadArray.TEXTURE_COORDINATE_2|QuadArray.NORMALS);
			quad.setCoordinate(0, new Point3d(-.5, 0, -.5));
			quad.setCoordinate(1, new Point3d(-.5, 0, +.5));
			quad.setCoordinate(2, new Point3d(+.5, 0, +.5));
			quad.setCoordinate(3, new Point3d(+.5, 0, -.5));
			
			quad.setNormal(0, new Vector3f(0, 1, 0));
			quad.setNormal(1, new Vector3f(0, 1, 0));
			quad.setNormal(2, new Vector3f(0, 1, 0));
			quad.setNormal(3, new Vector3f(0, 1, 0));
			
			quad.setTextureCoordinate(0, 0, new TexCoord2f(0.0f,0.0f));
			quad.setTextureCoordinate(0, 3, new TexCoord2f(1.0f,0.0f)); 
			quad.setTextureCoordinate(0, 2, new TexCoord2f(1.0f,-1.0f));
			quad.setTextureCoordinate(0, 1, new TexCoord2f(0.0f,-1.0f));
			return quad;
		}

		public static QuadArray createQuad(Vector3d e1, Vector3d e2, Vector3d e3, double w, double h) {
			Matrix3d m = new Matrix3d();
			m.setColumn(0, e1);
			m.setColumn(1, e2);
			m.setColumn(2, e3);
			
			Point3d p1 = new Point3d(-w/2, 0, -h/2);
			Point3d p2 = new Point3d(-w/2, 0, +h/2);
			Point3d p3 = new Point3d(+w/2, 0, +h/2);
			Point3d p4 = new Point3d(+w/2, 0, -h/2);
			m.transform(p1);
			m.transform(p2);
			m.transform(p3);
			m.transform(p4);
			
			
			QuadArray quad = new QuadArray(4, QuadArray.COORDINATES|QuadArray.NORMALS);
			quad.setCoordinate(0, p1);
			quad.setCoordinate(1, p2);
			quad.setCoordinate(2, p3);
			quad.setCoordinate(3, p4);
			
			Vector3f e2f = new Vector3f(e2);
			quad.setNormal(0, e2f);
			quad.setNormal(1, e2f);
			quad.setNormal(2, e2f);
			quad.setNormal(3, e2f);
			return quad;
		}
		
		private void createLabel(double w, double h) {
			if (tgLabel==null) {
				t3dLabel = new Transform3D();
				tgLabel = new TransformGroup();
				tgLabel.setCapabilityTo(TransformGroup.ALLOW_TRANSFORM_WRITE);
				Appearance appLabel=new Appearance();
				appLabel.setMaterial(new Material(black, black, black, white, 128));
				BranchGroup label = Utils3d.createFixedLegend("Diffraction screen", new Point3d(0, y, 0), .2f, appLabel, false);
				tgLabel.addChild(label);
				noSizeTg.addChild(tgLabel);
			}
			Vector3d v = new Vector3d(0, 0, 4.2*h/w);
			t3dLabel.set(v, w/8);
			tgLabel.setTransform(t3dLabel);
		}
		private void createCadre(double w, double h) {
			if (cadre1==null) {
				noSizeTg.addChild(cadre1=Utils3d.createCylinder(new Point3d(-w/2, 0, -h/2), new Point3d(-w/2, 0, h/2), .03, Utils3d.createApp(black), 10));
				noSizeTg.addChild(cadre2=Utils3d.createCylinder(new Point3d(w/2, 0, -h/2), new Point3d(w/2, 0, h/2), .03, Utils3d.createApp(black), 10));
				noSizeTg.addChild(cadre3=Utils3d.createCylinder(new Point3d(-w/2, 0, h/2), new Point3d(w/2, 0, h/2), .03, Utils3d.createApp(black), 10));
				noSizeTg.addChild(cadre4=Utils3d.createCylinder(new Point3d(w/2, 0, -h/2), new Point3d(-w/2, 0, -h/2), .03, Utils3d.createApp(black), 10));
			}
			else {
				Utils3d.changeCylinder(cadre1, new Point3d(-w/2, 0, -h/2), new Point3d(-w/2, 0, h/2));
				Utils3d.changeCylinder(cadre2, new Point3d(w/2, 0, -h/2), new Point3d(w/2, 0, h/2));
				Utils3d.changeCylinder(cadre3, new Point3d(-w/2, 0, h/2), new Point3d(w/2, 0, h/2));
				Utils3d.changeCylinder(cadre4, new Point3d(w/2, 0, -h/2), new Point3d(-w/2, 0, -h/2));
			}
		}
		private void createBaseRay(double y) {
			if (baseRay!=null) removeChild(baseRay);
			baseRay = Utils3d.createCylinder(new Point3d(), new Point3d(0, y, 0), .02, Utils3d.createApp(ColorConstants.yellow), 8);
			addChild(baseRay);
		}
		
		public void setSize(double w, double h) {
			this.w=w; this.h=h;
			Matrix3d m = new Matrix3d(w, 0, 0, 0, 1, 0, 0, 0, h); 
			Transform3D t = new Transform3D();
			t.set(m);
			resizeTg.setTransform(t);
			createCadre(w, h);
			createLabel(w, h);
		}
		public void setPos(double y) {
			this.y=y;
			OyO = new Vector3d(0, y, 0);
			Transform3D t = new Transform3D();
			t.set(OyO);
			transTg.setTransform(t);
			createBaseRay(y);
		}
		
		public boolean projPoint(Point3d v, Vector3d n, double d) {
			double t = d/(v.x*n.x+v.y*n.y+v.z*n.z);
			if (t<0) return false;
			v.scale(t);
			return true;
		}

		public Point.Double proj3dTo2d(Vector3d p) {
			if (p.x>w/2||-p.x>w/2||p.y>h/2||-p.y>h/2) return null;
			return new Point.Double(p.x/w, -p.z/h);
		}
	}
	
	public void setImage(Image ingIn) {
		if (texture==null || textureWidth!=ingIn.getWidth(null) || textureHeight!=ingIn.getHeight(null)) {
			textureWidth = ingIn.getWidth(null);
			textureHeight = ingIn.getHeight(null);
			texture = new Texture2D(Texture.BASE_LEVEL,Texture.RGBA, textureWidth, textureHeight);
			ic2d = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, (BufferedImage)ingIn, false, false);
			ic2d.setCapability(ImageComponent2D.ALLOW_IMAGE_WRITE);
			texture.setImage(0, ic2d);
			texture.setCapability(Texture2D.ALLOW_IMAGE_READ);
			texture.setEnable(true);
			texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
			texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
			app.setTexture(texture);
		}
		else {
			ic2d.set((BufferedImage)ingIn);
		}
	}
}

