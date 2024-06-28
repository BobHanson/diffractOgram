package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;
import javax.vecmath.Point3d;

import org.jmol.j3d.JmolWorldRendererI;
import org.jmol.script.T;

import javajs.util.Lst;
import javajs.util.P3d;

public class JmolCylinder extends JmolShape3D {

	private boolean isHollow; // not yet implemented
	private Lst<P3d> coords;

	public JmolCylinder(String name, double radius, double height, boolean isHollow, int xdiv, int ydiv, Appearance app) {
		super(name, app, JMOL_SHAPE_CYLINDER);
		this.isHollow = isHollow;
		vertices = new Point3d[4];
		vertices[0] = new Point3d(0, -height/2, 0);
		vertices[1] = new Point3d(0, height/2, 0);
		vertices[2] = new Point3d(0, 0, 0);
		vertices[3] = new Point3d(radius, 0, 0);
	}

	@Override
	public String renderScript(JmolWorldRendererI renderer) {
		// use direct DRAW creation and jmolVertices
		if (!getJmolVertices(renderer))
			return "";
		pt.sub2(jmolVertices[3], jmolVertices[2]);
		double r = pt.length();
		short colix = getJmolColor();
		if (coords == null) {
			coords = new Lst<>();
			coords.addLast(jmolVertices[0]);
			coords.addLast(jmolVertices[1]);
		}
		if (shape == null) {
			getThisID();
		    draw(new Object[][] { 
	           { "init", "jmolcylinder" },
	           { "thisID", thisID },
	           { isHollow ?  "cylinder" : "thisID", thisID },
	           { "width", Double.valueOf(r * 2) },
	           { "points", Integer.valueOf(0) },
	           { "coords", coords },
	           { "set", null },
	           { "color", Integer.valueOf(argb) },
	           { "translucentLevel", Double.valueOf(translucency) },
	           { "translucency", "translucent" },
	           { "token", Integer.valueOf(T.mesh) },
	           { "token", Integer.valueOf(T.nofill) },
	           { "thisID", null }
	        });
		    return "";
		}
		shape.width = r * 2;
		shape.vs[0].setT(jmolVertices[0]);
		shape.vs[1].setT(jmolVertices[1]);
		shape.colix = colix;			
		return "";

	}

//	return getThisID() 
//	+ " width " + r*2 + " " + jmolVertices[0] + jmolVertices[1]
//	+ getJmolDrawApp(true);
	
}