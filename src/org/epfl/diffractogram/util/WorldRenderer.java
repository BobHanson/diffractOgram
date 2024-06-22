package org.epfl.diffractogram.util;

import java.awt.Font;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import org.epfl.diffractogram.model3d.Univers;

public abstract class WorldRenderer {
	
	protected Univers univers;
	protected JPanel panel3d;
	
	public static boolean isJS = (/** @j2sNative true || */
	false);

	public WorldRenderer(JPanel panel3d, Univers univers) {
		this.panel3d = panel3d;
		this.univers = univers;
	}

	public static WorldRenderer createWorldRenderer(JPanel panel3d, Univers univers) {
		return (isJS ? new JSWorldRenderer(panel3d, univers) : new Java3DWorldRenderer(panel3d, univers));
	}

	public abstract void setEnvironment(TransformGroup reset, BranchGroup root);

	public abstract boolean isParallel();

	public abstract void setParallel(boolean b);

	public abstract void setBackgroundColor(Color3f bgColor);

	public abstract void cleanup();

	protected void applyTransform(Transform3D t3d) {
		univers.applyTransform(t3d);
	}

	public static Shape3D getTextShapeStatic(String s, Font font, int align, int path, Point3d rot,
			Appearance app) {
		return (isJS ? JSWorldRenderer.getTextShape(s,  font, align, path, rot, app)
				: Java3DWorldRenderer.getTextShape(s, font, align, path, rot, app));
	}


}