package org.jmol.j3d.geometry;

import javax.media.j3d.Transform3D;

import org.jmol.j3d.JmolWorldRendererI;
import org.jmol.shape.Mesh;

public class JmolVertexSet extends JmolShape3D {

	public JmolVertexSet(String name, Object mesh) {
		super(name, null, JMOL_SHAPE_VERTEX_SET);
		shape = (Mesh) mesh;
		if (shape == null)
			System.out.println(name + " " + mesh);
	}

	@Override
	public String renderScript(JmolWorldRendererI renderer) {
		// presumption here is that this is already created. 
		// all we are doing here is changing the vertices.
		Transform3D tr = renderer.getTransform(this);
		if (tr == null)
			return "";
		return recalcVertices(tr, Short.MAX_VALUE);
	}


}