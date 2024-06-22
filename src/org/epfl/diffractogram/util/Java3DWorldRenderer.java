package org.epfl.diffractogram.util;

import java.awt.AWTEvent;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
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
import javax.media.j3d.Node;
import javax.media.j3d.OrientedShape3D;
import javax.media.j3d.PickRay;
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

import com.sun.j3d.utils.universe.SimpleUniverse;

@SuppressWarnings("serial")
public class Java3DWorldRenderer extends WorldRenderer {

	private Canvas3D canvas3D;
	private SimpleUniverse u;
	private BranchGroup environment;
	private Background background;
	private BoundingSphere bounds;

	Java3DWorldRenderer(JPanel panel3d, Univers univers) {
		super(panel3d, univers);
		canvas3D = new Canvas3D(SimpleUniverse.getPreferredConfiguration()) {
			public void paint(Graphics g) {
				super.paint(g);
				Toolkit.getDefaultToolkit().sync();
			}
		};
		u = new SimpleUniverse(canvas3D);
		panel3d.add(canvas3D);
	}

	@Override
	public void setEnvironment(TransformGroup reset, BranchGroup root) {
		// create the environment (lights, background, ...)
		createEnvironment();
		environment.addChild(reset);

		// the behavior reacts on mouse events
		UniversBehavior behavior = new UniversBehavior();
		behavior.setSchedulingBounds(bounds);
		root.addChild(behavior);

		// also use mouse wheel to scale up/down
		canvas3D.addMouseWheelListener(new WheelMouseBehavior());

		u.getViewingPlatform().setNominalViewingTransform();

		// show the whole thing up
		u.addBranchGraph(environment);
		// TODO Auto-generated method stub

	}

	private void createEnvironment() {
		// Create the root of the branch graph
		environment = new BranchGroup();

		// Create a bounds for the background and lights
		bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY);

		// create the background
		background = new Background(new Color3f(1, 1, 1));
		background.setCapability(Background.ALLOW_COLOR_WRITE);
		background.setApplicationBounds(bounds);
		environment.addChild(background);

		// Set up the ambient light
		Color3f ambientColor = new Color3f(0.4f, 0.4f, 0.4f);
		AmbientLight ambientLightNode = new AmbientLight(ambientColor);
		ambientLightNode.setInfluencingBounds(bounds);
		environment.addChild(ambientLightNode);

		// Set up the directional lights
		Color3f light1Color = new Color3f(0.7f, 0.7f, 0.7f);
		Vector3f light1Direction = new Vector3f(1.0f, 1.0f, 1.0f);
		Color3f light2Color = new Color3f(0.7f, 0.7f, 0.7f);
		Vector3f light2Direction = new Vector3f(-1.0f, -1.0f, -1.0f);

		DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
		light1.setInfluencingBounds(bounds);
		environment.addChild(light1);

		DirectionalLight light2 = new DirectionalLight(light2Color, light2Direction);
		light2.setInfluencingBounds(bounds);
		environment.addChild(light2);

		BoundingLeaf boundingLeaf = new BoundingLeaf(bounds);
		environment.addChild(boundingLeaf);
	}

	private class WheelMouseBehavior implements MouseWheelListener {
		public void mouseWheelMoved(MouseWheelEvent e) {
			processMouseWheelEvent(e);
		}
	}

	@Override
	public boolean isParallel() {
		return u.getViewer().getView().getProjectionPolicy() == View.PARALLEL_PROJECTION;
	}

	@Override
	public void setParallel(boolean b) {
		u.getViewer().getView().setProjectionPolicy(b ? View.PARALLEL_PROJECTION : View.PERSPECTIVE_PROJECTION);
	}

	@Override
	public void setBackgroundColor(Color3f bgColor) {
		background.setColor(bgColor);
	}

	@Override
	public void cleanup() {
		u.cleanup();
	}

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
			applyTransform(t3d);
		}

		private void doZoom() {
			t3d.set(1.0 + dy / 100d);
			applyTransform(t3d);
			
			t3d.set(1.0 + dy / 100d);
			applyTransform(t3d);

		}
		
		private void doRotateXY() {
			t3d.rotX(dy * y_factor);
			applyTransform(t3d);
			t3d.rotY(dx * x_factor);
			applyTransform(t3d);

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

			return getPointedObject(univers.root, mousePos, mouseVec);

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
		applyTransform(t3d);
	}

	public static Shape3D getTextShape(String s, Font font, int align, int path, Point3d rot,
			Appearance app) {
		Shape3D textShape;
		if (rot == null) {
			textShape = new Shape3D();
		} else {
			OrientedShape3D t = new OrientedShape3D();
			t.setAlignmentMode(OrientedShape3D.ROTATE_ABOUT_POINT);
			t.setRotationPoint(new Point3f(rot));
			textShape = t;
		}
		Font3D f3d = new Font3D(font, new FontExtrusion());
		Text3D txt = new Text3D(f3d, s, new Point3f(), align, path);
		textShape.setGeometry(txt);
		textShape.setAppearance(app);	
		return textShape;
	}

}
