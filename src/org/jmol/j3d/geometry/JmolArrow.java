package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;
import javax.vecmath.Point3d;

import org.jmol.j3d.JmolWorldRendererI;
import org.jmol.shapespecial.DrawMesh;

public class JmolArrow extends JmolShape3D {

	
	public JmolArrow(String name, double radiusArrow, double lenArrow, double radius, float height, int precision,
			Appearance app) {
		super(name, app, JMOL_SHAPE_ARROW);

		vertices = new Point3d[4];
		vertices[0] = new Point3d(0, - height / 2, 0);
		vertices[1] = new Point3d(0, height / 2 + lenArrow, 0);
		vertices[2] = new Point3d(0, 0, 0);
		vertices[3] = new Point3d(radius, 0, 0);
	}
	
	@Override
	public String renderScript(JmolWorldRendererI renderer) {
		// use direct DRAW creation and jmol vertices
		if (!getJmolVertices(renderer))
			return "";

		double r = distance(2, 3);
		short colix = getJmolColor();
		
		if (shape == null) {
			getThisID();
			draw(new Object[][] { 
		           { "init", "jmolarrow" },
		           { "thisID", thisID },
		           { "width", Double.valueOf(r * 2) },
		           { "arrow", null },
		           { "points", Integer.valueOf(0) },
		           { "coord", jmolVertices[0] },
		           { "coord", jmolVertices[1] },
		           { "set", null },
		           { "color", Integer.valueOf(argb) },
		           { "translucentLevel", Double.valueOf(translucency) },
		           { "translucency", "translucent" },
		           { "thisID", null }
		    });
		    return "";
		}
		shape.vs[0].setT(jmolVertices[0]);
		shape.vs[1].setT(jmolVertices[1]);
		((DrawMesh) shape).width = r;
		shape.colix = colix;
		return "";
	}

//	DRAW init value=draw id 'axes:c_' width 0.03999999910593033 vector {-9.146900343714513E-25, 0.0, 1.5965973996408384E-26}{-4.775758641155923E-17, 0.38999997824430466, 4.167741517131408E-19} color [0.0 0.0 1.0]; bs=({})
//	DRAW thisID value=+PREVIOUS_MESH+ bs=({})
//	DRAW thisID value=axes:c_ bs=({})
//	DRAW width value=0.03999999910593033 bs=({})
//	DRAW arrow value=null bs=({})
//	DRAW points value=0 bs=({})
//	DRAW coord value={-9.146900343714513E-25, 0.0, 1.5965973996408384E-26} bs=({})
//	DRAW coord value={-4.775758641155923E-17, 0.38999997824430466, 4.167741517131408E-19} bs=({})
//	DRAW set value=null bs=({})
//	DRAW color value=-16776961 bs=({})

}
//
//	
//	String s = getThisID() + " width " + r * 2
//	// + jmolVertices[0] + jmolVertices[1]
//			+ " vector " + jmolVertices[0] + pt + getJmolDrawApp(true);
//	return s;

