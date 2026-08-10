package org.jmol.j3d.geometry;

import javax.media.j3d.Appearance;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.jmol.j3d.JmolWorldRendererI;
import org.jmol.shape.Mesh;
import org.jmol.shapespecial.Draw;

import javajs.util.P3d;
import javajs.util.T3d;

public class JmolUnitCell extends JmolBox {


	public JmolUnitCell(String name, float radius, Vector3d x, Vector3d y, Vector3d z, Appearance app) {
		super(name, radius, x, y, z, app, JMOL_SHAPE_UNITCELL);
	}

	final static String ucFunction = "function drawHKL(h, k, l, uc) {\n"//
			+ " if (!uc){ uc=[{0 0 0} {1 0 0} {0 1 0} {0 0 1} ] }\n" //
			+ "	xuc = uc\n" // for debugging
			
			+ " var p = hkl(uc h k l)\n" //
			+ " var pp = intersection(uc[1], p)\n"
			+ " var n = uc[1] - pp\n" +//
		    "   draw miller_p1 intersect unitcell @uc hkl unitcell @uc @h @k @l\n" + //
			"	draw miller_p2 offset @n intersect unitcell @uc hkl unitcell @uc @h @k @l\n" + //
			"	draw miller_pm @{uc[1]} @pp\n" + //
			"   #draw test unitcell @uc axes\n" + //
			"   print 'drawHKL ' + h + ' ' + k + ' ' + l + ' [' + uc+']'\n" +
			"}\n";
	
	private static boolean haveUCFunction = false;
	
	private int h, k, l;
	private boolean needsCalc;

	private Transform3D tr;
	
	
	public void setMillerPlanes(int h, int k, int l) {
		this.h = h;
		this.k = k;
		this.l = l;
		needsCalc = true;
	}
	
	@Override
	public String renderScript(JmolWorldRendererI renderer) {
		String s = super.renderScript(renderer);
		if (!haveUCFunction) {
			haveUCFunction = true;
			renderer.scriptWait(ucFunction);
		}
		if (needsCalc) {
			if (h == 0 && k == 0 && l == 0) {
				s += ";draw miller* delete";
			} else {
				tr = renderer.getTransform(this);
				if (tr != null) {
					String uc = getUCoabc();
					renderer.scriptWait("drawHKL(" + h + " " + k + " " + l + " " + uc + ")");
					needsCalc = false;
				}
			}
		} else {
			Draw d = super.getDraw();
			updateMesh(d, "miller_p1");
			updateMesh(d, "miller_p2");
			updateMesh(d, "miller_pm");
		}
		return s;
	}

	private void updateMesh(Draw d, String id) {
		Mesh m = d.getMesh(id);
		if (m == null)
			return;
		setShapeVertices(m, tr);
	}

	private String getUCoabc() {
		T3d[] vs = shape.getVertices();
		P3d o = transform(vs[0]);
		P3d a = transform(vs[1]);
		a.sub(o);
		P3d b = transform(vs[4]);
		b.sub(o);
		P3d c = transform(vs[3]);
		c.sub(o);
		return "["+ o + a + b + c + "]";
	}

	private final Point3d p = new Point3d(); 

	private P3d transform(T3d t) {
		p.set(t.x, t.y, t.z);
		return P3d.new3(p.x, p.y, p.z);
	}

	
	



}