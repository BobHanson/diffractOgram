package org.jmol.j3d;

import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;

import org.jmol.j3d.geometry.JmolShape3D;

public interface WorldRendererI {

	void renderNode(Shape3D shape);

	Object getViewer();

	Transform3D getTransform(JmolShape3D shape);

}
