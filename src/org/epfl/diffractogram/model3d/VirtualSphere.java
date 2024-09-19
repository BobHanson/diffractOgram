package org.epfl.diffractogram.model3d;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.epfl.diffractogram.DefaultValues;
import org.epfl.diffractogram.util.Colors;
import org.epfl.diffractogram.util.Utils3d;

public class VirtualSphere extends BranchGroup {
	TransformGroup sPositioned;
	public Point3d center;
	public double scaledRadius;
	public DefaultValues defaultValues;
	private Model3d model3d;
	
	public VirtualSphere(Model3d model3d,DefaultValues defaultValues) {
		setName("virtualsphere");
		this.model3d = model3d;
		this.defaultValues = defaultValues;
		center = new Point3d();
		sPositioned = model3d.univers.newWritableTransformGroup(null);

		setLambda(defaultValues.param_lambda);
		createSphere("vsphere");
		createLegend();
		if (!DefaultValues.isUnitSphere)
			createRadius();
		createRepere();
	}

	public static Appearance app(Color c) {
		Appearance app = new Appearance();
		app.setMaterial(new Material(Colors.magenta, Colors.black, Colors.magenta, Colors.white, 128));
		  app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST,0.7f));

		BufferedImage i = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		Graphics g = i.getGraphics();
		g.setColor(c);
		g.fillRect(0, 0, 64, 64);
		ImageComponent2D image = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, i, false, false);

		Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());
		texture.setImage(0, image);
		texture.setEnable(true);
		texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
		texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);

		app.setTexture(texture);
		app.setTextureAttributes(new TextureAttributes());

		app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.8f));

		PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		pa.setBackFaceNormalFlip(true);
		app.setPolygonAttributes(pa);

		return app;
	}

	private void createSphere(String name) {
		// was app(new Color(1f, .7f, .7f, .5f));
		Color3f c = new Color3f(1f, .7f, .7f);
		Appearance app = new Appearance();
		app.setMaterial(new Material(c, Colors.black, c, Colors.white, 128));
		app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.5f));
		Node s = model3d.univers.renderer.createSphere(name, 1, 100, false, app);
		Utils3d.setParents(s, sPositioned, this);

		if (DefaultValues.isUnitSphere) {
			BranchGroup incidentRay = model3d.univers.creator.createCylinder(model3d.univers, "s0", 
					new Point3d(0, -1, 0),
					new Point3d(0, 0, 0), .01, Colors.appOrange, 8);
			Utils3d.setParents(incidentRay, sPositioned, this);
		} else {
			BranchGroup incidentBeam = model3d.univers.creator.createCylinder(model3d.univers, "sourceray", 
					new Point3d(0, -1.5, 0),
					new Point3d(0, 1, 0), .005, Colors.appOrange, 8);
			Utils3d.setParents(incidentBeam, sPositioned, this);
			BranchGroup source = model3d.univers.creator.createCylinder(model3d.univers, "source", new Point3d(0, -1.5, 0),
					new Point3d(0, -1.3, 0), .025, Colors.appYellow, 8);
			Utils3d.setParents(source, sPositioned, this);
		}

	}

	private void createLegend() {
		Appearance app = new Appearance();
		app.setMaterial(new Material(Colors.magenta, Colors.black, Colors.magenta, Colors.white, 128));
		app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.7f));
		Node legend = model3d.univers.creator.createFixedLegend("vs1", (DefaultValues.isUnitSphere ? "Unit sphere" : "Ewald sphere"), new Point3d(0, -.9, 0), .05f, app, true);
		legend.setName("legend:ewald");
		Utils3d.setParents(legend, sPositioned, this);
	}

	private void createRadius() {
		Transform3D t3v1 = new Transform3D();
		Transform3D t3v2 = new Transform3D();
		t3v1.rotZ(3 * Math.PI / 4);
		t3v2.rotY(-Math.PI / 4);
		t3v1.mul(t3v2);
		Node l = model3d.univers.creator.createNamedVector("vsr", new Point3d(0, 0, 0), new Point3d(-.98, 0, 0),
				new Point3d(-.5, 0, .02), .2f, 0.03f, Colors.magenta, Colors.magenta, "1/" + DefaultValues.strLambda);
		l.setName("vector:lambda");
		Utils3d.setParents(l, model3d.univers.newWritableTransformGroup(t3v1), sPositioned, this);
	}

	private void createRepere() {
		if (DefaultValues.isUnitSphere)
			return;

		Transform3D t3dRepere = new Transform3D();
		t3dRepere.set(.3);
		Node r = model3d.univers.creator.createRepere("repereXYZ:", Colors.cyan, Colors.green, Colors.green, new String[] { "x", "y", "z" }, .15f, .03f, 0,
				0, new Vector3d(1, 0, 0), new Vector3d(0, 1, 0), new Vector3d(0, 0, 1), false);
		r.setName("repereXYZ:");
		Utils3d.setParents(r, model3d.univers.newWritableTransformGroup(t3dRepere), sPositioned, this);
	}

	public double lambdaToRadius(double lambda) {
		if (DefaultValues.isUnitSphere)
			return DefaultValues.scale;
		return DefaultValues.scale / lambda;
	}

	public void setLambda(double lambda) {
		scaledRadius = lambdaToRadius(lambda);
		center.set(0, -scaledRadius, 0);
		Transform3D t = new Transform3D();
		t.set(scaledRadius, new Vector3d(center));
		sPositioned.setTransform(t);
	}
}
