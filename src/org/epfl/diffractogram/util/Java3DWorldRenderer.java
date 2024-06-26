package org.epfl.diffractogram.util;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Enumeration;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingLeaf;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.OrientedShape3D;
import javax.media.j3d.PickRay;
import javax.media.j3d.QuadArray;
import javax.media.j3d.SceneGraphPath;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Text3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.epfl.diffractogram.model3d.Univers;
import org.epfl.diffractogram.model3d.Univers.Selectable;
import org.j3d.geom.Torus;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;

@SuppressWarnings("serial")
public class Java3DWorldRenderer extends WorldRenderer {

	private Canvas3D canvas3D;
	private SimpleUniverse j3dUniverse;
	private BranchGroup environment;
	private Background background;
	private BoundingSphere bounds;

	int test = 0;
	
	public Java3DWorldRenderer(JPanel panel3d, Univers univers) {
		super(panel3d, univers);
		canvas3D = new Canvas3D(SimpleUniverse.getPreferredConfiguration()) {
			public void paint(Graphics g) {
				super.paint(g);
				Toolkit.getDefaultToolkit().sync();
			}
		};
		j3dUniverse = new SimpleUniverse(canvas3D);
		panel3d.add(canvas3D);
	}

	@Override
	public void setEnvironment(TransformGroup tgReset) {
		
		// create the environment (lights, background, ...)
		createEnvironment();
		environment.addChild(tgReset);

		// the behavior reacts on mouse events
		UniversBehavior behavior = new UniversBehavior();
		behavior.setSchedulingBounds(bounds);
		univers.getRoot().addChild(behavior);

		// also use mouse wheel to scale up/down
		canvas3D.addMouseWheelListener(new WheelMouseBehavior());

		j3dUniverse.getViewingPlatform().setNominalViewingTransform();

		// show the whole thing up
		j3dUniverse.addBranchGraph(environment);
		// TODO Auto-generated method stub
		
		
		reset(tgReset);
		
	}

	/**
	 * Create the environment BranchGroup and add background, ambientLight, and two
	 * directional lights
	 */
	private void createEnvironment() {
		// Create the root of the branch graph
		environment = new BranchGroup();

		// Create a bounds for the background and lights
		bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY);

		// create the background
		background = new Background(new Color3f(1, 1, 1));
		background.setCapability(Background.ALLOW_COLOR_WRITE);
		background.setApplicationBounds(bounds);

		// Set up the ambient light
		Color3f ambientColor = new Color3f(0.4f, 0.4f, 0.4f);
		AmbientLight ambientLightNode = new AmbientLight(ambientColor);
		ambientLightNode.setInfluencingBounds(bounds);

		// Set up the directional lights
		Color3f light1Color = new Color3f(0.7f, 0.7f, 0.7f);
		Vector3f light1Direction = new Vector3f(1.0f, 1.0f, 1.0f);
		Color3f light2Color = new Color3f(0.7f, 0.7f, 0.7f);
		Vector3f light2Direction = new Vector3f(-1.0f, -1.0f, -1.0f);

		DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
		light1.setInfluencingBounds(bounds);

		DirectionalLight light2 = new DirectionalLight(light2Color, light2Direction);
		light2.setInfluencingBounds(bounds);

		BoundingLeaf boundingLeaf = new BoundingLeaf(bounds);
		
		environment.addChild(background);
		environment.addChild(ambientLightNode);
		environment.addChild(light1);
		environment.addChild(light2);
		environment.addChild(boundingLeaf);
	}

	private class WheelMouseBehavior implements MouseWheelListener {
		public void mouseWheelMoved(MouseWheelEvent e) {
			processMouseWheelEvent(e);
		}
	}

	@Override
	public boolean isParallel() {
		return j3dUniverse.getViewer().getView().getProjectionPolicy() == View.PARALLEL_PROJECTION;
	}

	@Override
	public void setParallel(boolean b) {
		j3dUniverse.getViewer().getView().setProjectionPolicy(b ? View.PARALLEL_PROJECTION : View.PERSPECTIVE_PROJECTION);
	}

	@Override
	public void setBackgroundColor(Color3f bgColor) {
		background.setColor(bgColor);
	}

	@Override
	public void cleanup() {
		j3dUniverse.cleanup();
	}

	/**
	 * Java only -- there is no need for tracking the mouse when using Jmol.
	 * 
	 *
	 */
	protected class UniversBehavior extends Behavior {

		private static final double x_factor = .02;
		private static final double y_factor = .02;

		private javax.media.j3d.WakeupOr mouseCriterion;

		protected Transform3D t3d;
		protected int x, y, x_last, y_last, dx, dy;

		@Override
		public void initialize() {
			t3d = new Transform3D();
			WakeupCriterion[] mouseEvents = new WakeupCriterion[3];
			mouseEvents[0] = new javax.media.j3d.WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED);
			mouseEvents[1] = new javax.media.j3d.WakeupOnAWTEvent(MouseEvent.MOUSE_DRAGGED);
			mouseEvents[2] = new javax.media.j3d.WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED);
			mouseCriterion = new javax.media.j3d.WakeupOr(mouseEvents);
			wakeupOn(mouseCriterion);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void processStimulus(Enumeration criteria) {
			javax.media.j3d.WakeupCriterion wakeup;
			while (criteria.hasMoreElements()) {
				wakeup = (WakeupCriterion) criteria.nextElement();
				if (wakeup instanceof WakeupOnAWTEvent) {
					AWTEvent[] events = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
					for (int i = 0; i < events.length; i++) {
						if (events[i] instanceof MouseEvent) {
							processMouseEvent((MouseEvent) events[i]);
						}
					}
				}
			}
			wakeupOn(mouseCriterion);
		}

		public void processMouseEvent(MouseEvent e) {

			switch (e.getID()) {
			case MouseEvent.MOUSE_PRESSED:
				x = x_last = e.getX();
				y = y_last = e.getY();
				Selectable s = getPointedObject();
				if (s != null)
					s.click();
				break;
			case MouseEvent.MOUSE_DRAGGED:
				x = e.getX();
				y = e.getY();
				dx = x - x_last;
				dy = y - y_last;
				if (e.isAltDown()) {
					doZoom();
				} else if (e.isShiftDown()) {
					doZRotate();
				} else if (e.isMetaDown() || e.isControlDown() || e.isAltGraphDown()) {
					// TODO selection multiple
					doZoom();
				} else {
					doRotateXY();
				}
				x_last = x;
				y_last = y;
				break;
			case MouseEvent.MOUSE_RELEASED:
				break;
			}

		}

		private void doZRotate() {
			int w = canvas3D.getWidth();
			int h = canvas3D.getHeight();
			Vector2d p1 = new Vector2d(x_last - w / 2, y_last - h / 2);
			Vector2d p2 = new Vector2d(x - w / 2, y - h / 2);
			Vector2d p3 = new Vector2d(1, 0);
			boolean neg = p1.angle(p3) < p2.angle(p3);
			neg = y - h / 2 > 0 ? !neg : neg;
			double alpha = p1.angle(p2);
			t3d.rotZ(neg ? alpha : -alpha);
			univers.applyTransform(t3d);
		}

		private void doZoom() {
			t3d.set(1.0 + dy / 100d);
			univers.applyTransform(t3d);
		}

		private void doRotateXY() {
			t3d.rotX(dy * y_factor);
			univers.applyTransform(t3d);
			t3d.rotY(dx * x_factor);
			univers.applyTransform(t3d);

		}

		public Selectable getPointedObject() {
			Point3d mousePos = new Point3d();
			Transform3D plateTovWorldT3d = new Transform3D();
			Point3d eyePos = new Point3d();

			canvas3D.getCenterEyeInImagePlate(eyePos);
			canvas3D.getPixelLocationInImagePlate(x, y, mousePos);
			canvas3D.getImagePlateToVworld(plateTovWorldT3d);

			plateTovWorldT3d.transform(eyePos);
			plateTovWorldT3d.transform(mousePos);

			Vector3d mouseVec;
			if (isParallel()) {
				// not
				mouseVec = new Vector3d(0.f, 0.f, -1.f);
			} else {
				mouseVec = new Vector3d();
				mouseVec.sub(mousePos, eyePos);
				mouseVec.normalize();
			}

			return getPointedObject(univers.getRoot(), mousePos, mouseVec);

		}

		Selectable getPointedObject(BranchGroup root, Point3d mousePos, Vector3d mouseVec) {
			PickRay pickRay = new PickRay();
			pickRay.set(mousePos, mouseVec);
			SceneGraphPath[] sceneGraphPath = root.pickAllSorted(pickRay);
			if (sceneGraphPath != null) {
				for (int j = 0; j < sceneGraphPath.length; j++) {
					if (sceneGraphPath[j] != null) {
						Node node = sceneGraphPath[j].getObject();
						if (node instanceof Shape3D) {
							try {
								double dist[] = { 0.0 };
								boolean isRealHit = ((Shape3D) node).intersect(sceneGraphPath[j], pickRay, dist);
								if (isRealHit) {
									Object userData = node.getUserData();
									if (userData != null && userData instanceof Selectable) {
										return (Selectable) userData;
									}
								}
							} catch (Exception e) {
							}
						}
					}
				}
			}
			return null;
		}

	}

	void processMouseWheelEvent(MouseWheelEvent e) {
		int i;
		if (e.getWheelRotation() == 0)
			i = 0;
		else
			i = e.getWheelRotation() / Math.abs(e.getWheelRotation());
		Transform3D t3d = new Transform3D();
		t3d.set(1.0 + ((double) i) / 10d);
		univers.applyTransform(t3d);
	}

	public BranchGroup createTextShape(String name, BranchGroup bg, String s, Point3d pos, float size, Font font, int align,
			int path, Point3d rotPoint, Appearance app) {
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		Shape3D textShape;
		if (rotPoint == null) {
			// These will rotate with the object
			// Reciprocal lattice, Ewald Sphere, Precision mask, 1/lambda
			textShape = new Shape3D();
		} else {
			// x y z a b c a* b* c*
			// rot is (0 0 0)
			OrientedShape3D os = new OrientedShape3D();
			os.setAlignmentMode(OrientedShape3D.ROTATE_ABOUT_POINT);
			os.setRotationPoint(new Point3f(rotPoint));
			textShape = os;
		}
		Font3D f3d = new Font3D(font, new FontExtrusion());
		// switch from java.awt.Label.* to Text3D.*
		switch (align) {
		case Label.CENTER:
			align = Text3D.ALIGN_CENTER;
			break;
		case Label.LEFT:
			align = Text3D.ALIGN_FIRST;
			break;
		case Label.RIGHT:
			align = Text3D.ALIGN_LAST;
			break;
		}
		switch (path) {
		case KeyEvent.VK_LEFT:
			path = Text3D.PATH_LEFT;
			break;
		case KeyEvent.VK_RIGHT:
			path = Text3D.PATH_RIGHT;
			break;
		case KeyEvent.VK_DOWN:
			path = Text3D.PATH_DOWN;
			break;
		case KeyEvent.VK_UP:
			path = Text3D.PATH_UP;
			break;
		}
		Text3D txt = new Text3D(f3d, s, new Point3f(), align, path);
		textShape.setGeometry(txt);
		textShape.setAppearance(app);
		textShape.setName(name);

		if (rotPoint == null) {
			Transform3D trotX90 = new Transform3D();
			trotX90.rotX(Math.PI / 2);

			Transform3D tsize = new Transform3D();
			tsize.set(size);

			Transform3D tpos = new Transform3D();
			tpos.set(new Vector3d(pos));

			Utils3d.setParents(textShape, new TransformGroup(trotX90), new TransformGroup(tsize),
					new TransformGroup(tpos), bg);
		} else {
			Transform3D tsizepos = new Transform3D();
			tsizepos.set(size, new Vector3d(pos));
			Utils3d.setParents(textShape, new TransformGroup(tsizepos), bg);
		}

		return bg;
	}

	public Node createTorus(String name, double innerRadius, double outerRadius, int innerFaces, int outerFaces,
			Appearance app) {
		Node n = new Torus((float) innerRadius, (float) outerRadius, innerFaces, outerFaces, app);
		n.setName(name);
		return n;
	}

	public Node createBox(String name, double dx, double dy, double dz, Appearance app) {
		Node n = new Box((float) dx, (float) dy, (float) dz, app);
		n.setName(name);
		return n;
	}

	public Node createCylinder(String name, double radius, double height, boolean isHollow, int xdiv, int ydiv,
			Appearance app) {
		int flags = Cylinder.GENERATE_NORMALS | (isHollow ? Cylinder.GENERATE_TEXTURE_COORDS : 0);
		Cylinder c = new Cylinder((float) radius, (float) height, flags, xdiv, ydiv, app);
		if (isHollow) {
			c.removeChild(c.getShape(Cylinder.BOTTOM));
			c.removeChild(c.getShape(Cylinder.TOP));
		} else {
			c.setCapability(Primitive.ENABLE_APPEARANCE_MODIFY);
			c.getChild(0).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
			c.getChild(1).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
			c.getChild(2).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		}
		c.setName(name);
		return c;
	}

	public Node createSphere(String name, double radius, int divs, boolean isAtom, Appearance app) {
		Sphere s = new Sphere((float) radius, Sphere.GENERATE_NORMALS, divs, app);
		s.getShape().setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		s.setName(name);
		return s;
	}	

	public TransformGroup createArrow(String name, TransformGroup tg, double radiusArrow, double lenArrow, double radius,
			float height, int precision, Appearance app) {
		Node cone = new Cone((float) radiusArrow, (float) lenArrow, Cylinder.GENERATE_NORMALS, precision, 1, app);
		Node cyl = new Cylinder((float) radius, height, Cylinder.GENERATE_NORMALS, precision, 1, app);
		Utils3d.setParents(cone,
				Utils3d.getVectorTransformGroup(0, height / 2f + lenArrow / 2f, 0, null),
				tg);
		tg.addChild(cyl);
		tg.setName(name);
		return tg;
	}

	public Node createQuad(String name, QuadArray quad, Appearance app) {
		return new Shape3D(quad, app);
	}

	@Override
	public void reset(TransformGroup reset) {
		Transform3D t3d = new Transform3D();
		t3d.set(new Vector3d(0, 0, -5), .2);
		reset.setTransform(t3d);
		setParallel(false);
	}

	@Override
	public void notifyRemove(Group parent, Node child) {
//		if (child == null)
//			return;
		//System.out.println("removed " + child.getName() + " from " + parent.getName());
	}

	@Override
	public void notifyAdd(Group parent, Node child) {
//		System.out.println("added " + child.getName() + " to " + parent.getName());
//		switch(parent.getName()) {
//		case "root":
//			this.mapRoot.put(child.getName(), child);
//			break;
//		}
	}

	@Override
	public void notifyRemoveAll(Group g) {
//		Enumeration<? extends Node> e = g.getAllChildren();
//		while (e.hasMoreElements()) {
//			System.out.println("removed " + e.nextElement().getName() + " from " + g.getName());
//		}
	}

	@Override
	public TransformGroup newTransformGroup(Transform3D t3d) {
		return (t3d == null ? new TransformGroup() : new TransformGroup(t3d));
	}
	
}
