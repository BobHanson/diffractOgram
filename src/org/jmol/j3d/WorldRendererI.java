package org.jmol.j3d;

import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;

public interface WorldRendererI {

	void renderNode(Shape3D shape);

	Node getRoot();

	Transform3D getTopTransform();

	Object getViewer();

}
