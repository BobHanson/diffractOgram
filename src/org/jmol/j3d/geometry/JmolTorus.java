package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;
import javax.media.j3d.Transform3D;

import org.jmol.j3d.JmolWorldRendererI;

import javajs.util.PT;

public class JmolTorus extends JmolShape3D {

	private double innerRadius;
	private double outerRadius;

	/**
	 * from org.j3d.geom:
	 * 
	 * The outer radius is the radius of the center of the tube that forms the
	 * torus.The torus has the outer radius in the X-Z plane and it increments along
	 * the positive Y axis. The loop starts at the origin on the positive X axis and
	 * rotates counter-clockwise when looking down the -Y axis towards the X-Z
	 * plane.
	 * 
	 * So then:
	 * 
	 * <pre>
	 *
	 *                                                      
	 *                                                      .---x
	 *    (----------(----------0----------)----------)     |
	 *                         |--------ro------|           z
	 *                                     |-ri-|
	 *                      r1 |--------ro + ri-------|                  
	 *                      r2 |--ro - ri--|                  
	 *                      
	 *                      ri is also the plane distance {0 1 0 d}
	 * </pre>
	 */
	public JmolTorus(String name, double innerRadius, double outerRadius, int innerFaces, int outerFaces,
			Appearance app) {
		super(name, app, JMOL_SHAPE_TORUS);
		this.innerRadius = innerRadius;
		this.outerRadius = outerRadius;
		isosurfaceCount = 4;
	}

	static String torusTemplate = "" 
			+ " isosurface ID '$0_1' plane {0 1 0 -$3};\n"
			+ "	isosurface slab within $1 {0 0 0} slab within -$2 {0 0 0};\n"
			+ "	isosurface ID '$0_2' plane {0 1 0 $3};\n"
			+ "	isosurface slab within $1 {0 0 0} slab within -$2 {0 0 0};\n"
			+ "	isosurface ID '$0_3' center {0 0 0} sphere $1;\n"
			+ "	isosurface slab {0 -1 0 -$3} slab {0 1 0 -$3};\n"
			+ "	isosurface ID '$0_4' center {0 0 0} sphere $2;\n"
			+ "	isosurface slab {0 -1 0 -$3} slab {0 1 0 -$3};\n" + "";

	protected String setTemplate(String template, String name, double... vars) {
		String s = PT.rep(template, "$0", name);
		for (int i = vars.length; --i >= 0;) {
			s = PT.rep(s, "$" + (i + 1), "" + vars[i]);
		}
		return s;
	}

	@Override
	public String renderScript(JmolWorldRendererI renderer) {
		// use DRAW script with j3d coords and pass transform to Jmol
		Transform3D tr = renderer.getTransform(this);
		if (tr == null) {
			return "";
		}
		if (shapes == null) {
			double r1 = outerRadius + innerRadius;
			double r2 = outerRadius - innerRadius;
			double d = innerRadius;
			getThisID();
			scriptShape(setTemplate(torusTemplate, thisID, r1, r2, d));
		}
		recalcVertices(tr, Short.MIN_VALUE);
		return "";

	}

}