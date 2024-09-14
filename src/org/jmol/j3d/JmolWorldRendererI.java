package org.jmol.j3d;

import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;

public interface JmolWorldRendererI {

	void renderNode(Shape3D shape);

	Object getViewer();

	Transform3D getTransform(Node shape);

	void scriptWait(String s);

}
