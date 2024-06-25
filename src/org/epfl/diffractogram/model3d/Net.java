package org.epfl.diffractogram.model3d;

import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.epfl.diffractogram.diffrac.DefaultValues;
import org.epfl.diffractogram.diffrac.Lattice;
import org.epfl.diffractogram.transformations.OrientationClass;
import org.epfl.diffractogram.transformations.OrientationClass.OrientationObject;
import org.epfl.diffractogram.transformations.PrecessionClass;
import org.epfl.diffractogram.transformations.PrecessionClass.PrecessionObject;
import org.epfl.diffractogram.util.Calc;
import org.epfl.diffractogram.util.WorldRenderer;

/**
 * the Net class maintains lattice points in the displayed reciprocal lattice,
 * the goniometer head, and the floating label of the Net, all of which share
 * its orientation.
 * 
 */
public class Net extends BranchGroup implements ColorConstants {
	public GonioHead gonioHead;
	public OrientationObject orientationObject;
	public PrecessionObject precessionObject;
	private BranchGroup netLabel;
	public BranchGroup netRoot;
	public Point3d[][][] points;
	private BranchGroup[][][] atoms;
	private boolean[][][] isSelected;
	private boolean[][][] dontdraw;
	private float[][][] intensity;
	public boolean[][][] isProjected;
	private static Appearance defaultApp, redApp, greenApp;
	private Vector3d a, b, c;
	public int x, y, z;
	public int xMax, yMax, zMax;
	private BranchGroup directRepere;
	private boolean directShowed = false;
	private DefaultValues defaultValues;
	private Univers univers;

	public Net(Univers univers, OrientationClass orientationClass, PrecessionClass precessionClass,
			DefaultValues defaultValues, Vector3d a, Vector3d b, Vector3d c, int x, int y, int z) {
		this.setName("net");
		this.univers = univers;
		this.defaultValues = defaultValues;
		setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		if (defaultApp == null) {
			defaultApp = Utils3d.newAppearance("color:blue");
			defaultApp.setMaterial(new Material(blue, black, blue, white, 128));
		}
		if (redApp == null) {
			redApp = Utils3d.newAppearance("color:red");
			redApp.setMaterial(new Material(red, black, red, white, 128));
		}
		if (greenApp == null) {
			greenApp = Utils3d.newAppearance("color:green");
			greenApp.setMaterial(new Material(green, black, green, white, 128));
		}

		TransformGroup tgOmegaOnly = univers.newTransformGroup(null);
		tgOmegaOnly.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		orientationObject = orientationClass.new OrientationObject(tgOmegaOnly);
		orientationObject.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		orientationObject.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);

		precessionObject = precessionClass.new PrecessionObject();
		precessionObject.addChild(orientationObject);

		createNet(a, b, c, x, y, z);

		univers.addNotify(this, precessionObject);
		univers.addNotify(this, gonioHead = new GonioHead(univers, orientationClass));

		KeyboardFocusManager kbfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		kbfm.addKeyEventDispatcher(new MyKeyboardManager());
	}

	public class MyKeyboardManager extends DefaultKeyboardFocusManager {
		public boolean dispatchKeyEvent(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_F1 && e.getID() == KeyEvent.KEY_PRESSED) {
				if (directShowed) {
					univers.removeNotify(netRoot, directRepere);
				} else {
					univers.addNotify(netRoot, directRepere);
				}
				directShowed = !directShowed;
			}
			return super.dispatchKeyEvent(e);
		}
	}

	public BranchGroup putAtom(Vector3d v, Color3f c) {
		BranchGroup a = (BranchGroup) createAtom(v, Utils3d.createApp(c), defaultValues.dotSize3d);
		univers.addNotify(netRoot, a);
		return a;
	}

	static int atomid;

	private BranchGroup createAtom(Vector3d v, Appearance app, float dotSize3d) {
		BranchGroup bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		TransformGroup tg = Utils3d.getVectorTransformGroup(v.x, v.y, v.z, null);
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		++atomid;
		Utils3d.setParents(univers.renderer.createSphere("atom:sphere" + atomid, dotSize3d, 10, true, app), tg, bg);
		bg.setName("atom:" + atomid);
		return bg;
	}

	private BranchGroup createAtom(Vector3d v, float dotSize3d) {
		return createAtom(v, defaultApp, dotSize3d);
	}

	private void changeAtomApp(BranchGroup a, Appearance app) {
		Utils3d.getShapeChild(a).setAppearance(app);
	}

	public synchronized void createNet(Vector3d a, Vector3d b, Vector3d c, int x, int y, int z) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.x = x;
		this.y = y;
		this.z = z;

		boolean special = false;
		if (Math.abs(a.length() - b.length()) < 0.001 && Math.round(b.angle(a) * 180 / Math.PI) == 60
				&& Math.round(b.angle(c) * 180 / Math.PI) == 90 && Math.round(c.angle(a) * 180 / Math.PI) == 90)
			special = true;

		xMax = x * (special ? (2 * x + 1) : x);
		yMax = y * (special ? (2 * y + 1) : y);
		zMax = z;

		points = new Point3d[2 * xMax + 1][2 * yMax + 1][2 * zMax + 1];
		atoms = new BranchGroup[2 * xMax + 1][2 * yMax + 1][2 * zMax + 1];
		isSelected = new boolean[2 * xMax + 1][2 * yMax + 1][2 * zMax + 1];
		dontdraw = new boolean[2 * xMax + 1][2 * yMax + 1][2 * zMax + 1];
		intensity = new float[2 * xMax + 1][2 * yMax + 1][2 * zMax + 1];
		isProjected = new boolean[2 * xMax + 1][2 * yMax + 1][2 * zMax + 1];
		if (netRoot != null)
			univers.removeNotify(orientationObject, netRoot);

		for (int i = 0; i < points.length; i++)
			for (int j = 0; j < points[i].length; j++)
				for (int k = 0; k < points[i][j].length; k++) {
					points[i][j][k] = null;
					dontdraw[i][j][k] = true;
				}

		netRoot = new BranchGroup();
		netRoot.setName("netroot");
		netRoot.setCapability(BranchGroup.ALLOW_DETACH);
		netRoot.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		netRoot.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

		for (int i = -x; i <= x; i++) {
			for (int j = -y; j <= y; j++) {
				for (int k = -z; k <= z; k++) {
					// dontdraw[i+xMax][j+yMax][k+zMax] = true;
					// dontdraw[i+xMax][j+yMax][k+zMax] = ((i!=-x && i!=x) || (j!=-y && j!=y) ||
					// (k!=-z && k!=z));
					// dontdraw[i+xMax][j+yMax][k+zMax] = (i!=-x && i!=x && j!=-y && j!=y && k!=-z
					// && k!=z);
					// dontdraw[i+xMax][j+yMax][k+zMax] = !(i==-x && i!=x && j!=-y && j!=y && k!=-z
					// && k!=z);
					// dontdraw[i+xMax][j+yMax][k+zMax] = (j!=-y && j!=y && k!=-z && k!=z);
					boolean v = !(i != -x && i != x && j != -y && j != y && k != -z && k != z);
					createPoint(i, j, k, v);
					if (special) {
						createPoint(-(i + j), i, k, false);
						createPoint(j, -(i + j), k, false);
					}
				}
			}
		}

		createLegend();
		createRepere();
		createTranspBox();
		netRoot.compile();
		univers.addNotify(orientationObject, netRoot);
	}

	private void createPoint(int i, int j, int k, boolean visible) {
		Vector3d v = new Vector3d();
		v.scaleAdd(defaultValues.scale * i, a, v);
		v.scaleAdd(defaultValues.scale * j, b, v);
		v.scaleAdd(defaultValues.scale * k, c, v);

		BranchGroup atom = createAtom(v, defaultValues.dotSize3d);
		points[i + xMax][j + yMax][k + zMax] = new Point3d(v);
		atoms[i + xMax][j + yMax][k + zMax] = atom;
		isSelected[i + xMax][j + yMax][k + zMax] = false;
		intensity[i + xMax][j + yMax][k + zMax] = Calc.calcIntensity(a, b, c, i, j, k);
		dontdraw[i + xMax][j + yMax][k + zMax] = !visible;
		isProjected[i + xMax][j + yMax][k + zMax] = false;
		if (visible)
			univers.addNotify(netRoot, atom);
	}

	public void createLegend() {
		double h = defaultValues.scale * zMax * c.getZ() + 1;
		Transform3D t3l = new Transform3D();
		t3l.rotZ(Math.PI / 2);
		TransformGroup tgl = univers.newTransformGroup(t3l);
		Node txt = univers.creator.createFixedLegend("Reciprocal lattice", new Point3d(0, 0, h + .2), .1f,
				Utils3d.createApp(blue), true);
		txt.setName("leg:reclatt");
		tgl.addChild(txt);
		txt = univers.creator.createFixedLegend("points", new Point3d(0, 0, h), .1f, Utils3d.createApp(blue), true);
		txt.setName("leg:points");
		tgl.addChild(txt);
		if (netLabel != null)
			univers.removeNotify(this, netLabel);
		netLabel = new BranchGroup();
		netLabel.setName("netlabel");
		netLabel.setCapability(BranchGroup.ALLOW_DETACH);
		netLabel.addChild(tgl);
		univers.addNotify(this, netLabel);
	}

	public void createRepere() {
		BranchGroup bb = new BranchGroup();
		bb.setName("bb");
		bb.setCapability(BranchGroup.ALLOW_DETACH);
		Node rep = univers.creator.createRepere(cyan, blue, null, new String[] { "a*", "b*", "c*" }, .15f, .02f,
				defaultValues.dotSize, -defaultValues.dotSize, (Vector3d) Utils3d.mul(a, defaultValues.scale),
				(Vector3d) Utils3d.mul(b, defaultValues.scale), (Vector3d) Utils3d.mul(c, defaultValues.scale));
		rep.setName("rep:abc*");
		univers.addNotify(bb, rep);
		univers.addNotify(netRoot, bb);

		directRepere = new BranchGroup();
		directRepere.setName("directrepere");
		directRepere.setCapability(BranchGroup.ALLOW_DETACH);
		Vector3d[] r = Lattice.reciprocal(a, b, c);
		r[0].normalize();
		r[0].scale(.3);
		r[1].normalize();
		r[1].scale(.3);
		r[2].normalize();
		r[2].scale(.3);
		rep = univers.creator.createRepere(red, red, null, new String[] { "a", "b", "c" }, .15f, .02f, defaultValues.dotSize,
				-defaultValues.dotSize, (Vector3d) Utils3d.mul(r[0], defaultValues.scale),
				(Vector3d) Utils3d.mul(r[1], defaultValues.scale), (Vector3d) Utils3d.mul(r[2], defaultValues.scale));
		rep.setName("repere:abc");
		univers.addNotify(directRepere, rep);
		if (directShowed) {
			netRoot.addChild(directRepere);
		}
	}

	public void createTranspBox() {
		Appearance app = new Appearance();
		// was app.setMaterial(new Material(white, blue, black, blue, 120.0f));
		app.setMaterial(new Material(blue, black, blue, white, 120.0f));
		
		TransparencyAttributes transp = new TransparencyAttributes(TransparencyAttributes.NICEST, .90f);
		
		
		app.setTransparencyAttributes(transp);
		Transform3D t3d = new Transform3D();
		Matrix3d matrix = new Matrix3d();
		matrix.setColumn(0, (Vector3d) Utils3d.mul(a, x));
		matrix.setColumn(1, (Vector3d) Utils3d.mul(b, y));
		matrix.setColumn(2, (Vector3d) Utils3d.mul(c, z));
		t3d.set(matrix);
		Node box = univers.renderer.createBox("netbox", 2, 2, 2, app);
		Utils3d.setParents(box, univers.newTransformGroup(t3d), netRoot);
	}

	/**
	 * BH: Can't see why this would be a static subclass of Net.
	 *
	 */
	public static class GonioHead extends BranchGroup {
		private OrientationObject orientationGonio;
		private Transform3D t3d;
		private TransformGroup tg;

		public GonioHead(Univers univers, OrientationClass orientationClass) {
			setName("goinohead");

			TransformGroup tgOmegaOnly = univers.newTransformGroup(null);
			tgOmegaOnly.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			orientationGonio = orientationClass.new OrientationObject(tgOmegaOnly);
			t3d = new Transform3D();
			tg = univers.newTransformGroup(t3d);
			tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			Utils3d.setParents(orientationGonio, tg, this);
			Utils3d.setParents(orientationGonio.tgOmegaOnly, tg, this);
			buildGadjet(univers, orientationGonio);
		}

		public void setY(double y) {
			t3d.set(new Vector3d(0, -y, 0));
			tg.setTransform(t3d);
		}

		private void buildGadjet(Univers univers, OrientationObject gonio) {
			Node ring = univers.renderer.createTorus("ring", .05, .6, 10, 50, Utils3d.createApp(yellow));
			Node sample = univers.renderer.createBox("stage", 0.2, 0.2, 0.1, Utils3d.createApp(blue));
			Node pin = univers.creator.createCylinder(univers, "pin", new Point3d(), new Point3d(0, 0, -.4), .02,
					Utils3d.createApp(orange), 5);
			Utils3d.setParents(ring, gonio.tgOmegaOnly);

			// sample crystal
			Utils3d.setParents(sample, Utils3d.getVectorTransformGroup(0, 0, -.4, null), gonio);

			// sample pin
			Utils3d.setParents(pin, gonio);
		}
	}

	public synchronized void setLattice(Lattice l) {
		createNet(l.x, l.y, l.z, x, y, z);
	}

	public synchronized void setCrystalSize(int x, int y, int z) {
		createNet(a, b, c, x, y, z);
	}

	public synchronized Point3d getPoint(int h, int k, int l) {
		return points[h + xMax][k + yMax][l + zMax];
	}

	public synchronized void highlight(int h, int k, int l) {
		if (!isSelected[h + xMax][k + yMax][l + zMax]) {
			if (dontdraw[h + xMax][k + yMax][l + zMax])
				univers.addNotify(netRoot, atoms[h + xMax][k + yMax][l + zMax]);
			changeAtomApp(atoms[h + xMax][k + yMax][l + zMax], redApp);
			isSelected[h + xMax][k + yMax][l + zMax] = true;
		}
	}

	public synchronized void unHighlight(int h, int k, int l) {
		if (isSelected[h + xMax][k + yMax][l + zMax]) {
			changeAtomApp(atoms[h + xMax][k + yMax][l + zMax], defaultApp);
			if (dontdraw[h + xMax][k + yMax][l + zMax])
				univers.removeNotify(netRoot, atoms[h + xMax][k + yMax][l + zMax]);
			isSelected[h + xMax][k + yMax][l + zMax] = false;
		}
	}

	public synchronized void highlightGreen(int h, int k, int l) {
		changeAtomApp(atoms[h + xMax][k + yMax][l + zMax], greenApp);
	}

	public synchronized float intensity(int h, int k, int l) {
		return intensity[h + xMax][k + yMax][l + zMax];
	}
}
