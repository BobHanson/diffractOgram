package org.epfl.diffractogram.model3d;

import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.epfl.diffractogram.DefaultValues;
import org.epfl.diffractogram.util.Calc;
import org.epfl.diffractogram.util.ColorConstants;
import org.epfl.diffractogram.util.Lattice;
import org.epfl.diffractogram.util.Utils3d;

/**
 * the Net class maintains lattice points in the displayed reciprocal lattice,
 * the goniometer head, and the floating label of the Net, all of which share
 * its orientation.
 * 
 */
public class Net extends BranchGroup {
	
	/**
	 * TRUE to develop the reciprocal lattice as we go;
	 * FALSE for orginal functionality
	 */
	final static boolean developNet = true;

	
//	public UnitCell unitCell2;
	public TransformGroup orientationObject;
	public TransformGroup precessionObject;
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
	BranchGroup directRepere;


	private BranchGroup unitCell;
	private boolean showDirect, showUnitCell;
	private DefaultValues defaultValues;
	private Univers univers;
	
	
	double scaling;
	private Model3d model3d;
	private TransformGroup unitcellObject;
	public Lattice rl;


	private Transform3D directTR;
	public Transform3D directTRInv;
	
	public Net(Model3d model3d, DefaultValues defaultValues) {
	
		rl = defaultValues.lattice.reciprocal();
		this.model3d = model3d;
		this.univers = model3d.univers;
		int x = defaultValues.crystalX;
		int y = defaultValues.crystalY; 
		int z = defaultValues.crystalZ;
		this.setName("net");
		this.defaultValues = defaultValues;
		setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		if (defaultApp == null) {
			defaultApp = Utils3d.newAppearance("color:blue");
			defaultApp.setMaterial(new Material(ColorConstants.blue, ColorConstants.black, 
					ColorConstants.blue, ColorConstants.white, 128));
		}
		if (redApp == null) {
			redApp = Utils3d.newAppearance("color:red");
			redApp.setMaterial(new Material(ColorConstants.red, ColorConstants.black, 
					ColorConstants.red, ColorConstants.white, 128));
		}
		if (greenApp == null) {
			greenApp = Utils3d.newAppearance("color:green");
			greenApp.setMaterial(new Material(ColorConstants.green, ColorConstants.black, 
					ColorConstants.green, ColorConstants.white, 128));
		}
		orientationObject = model3d.orientation.addOrientationObject(univers.newWritableTransformGroup(null));
		orientationObject.setName("orientation");
		orientationObject.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		orientationObject.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
//		tgOmegaOnly.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		precessionObject = model3d.precession.addPrecessionObject(univers.newWritableTransformGroup(null));
		precessionObject.setName("precession");
		precessionObject.addChild(orientationObject);
		precessionObject.addChild(unitcellObject);

		createNet(rl.x, rl.y, rl.z, x, y, z);

		univers.addNotify(this, precessionObject);
		KeyboardFocusManager kbfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		kbfm.addKeyEventDispatcher(new MyKeyboardManager());
	}

	public class MyKeyboardManager extends DefaultKeyboardFocusManager {
		public boolean dispatchKeyEvent(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_F1 && e.getID() == KeyEvent.KEY_PRESSED) {
				toggleDirect();
			}
			if (e.getKeyCode() == KeyEvent.VK_F2 && e.getID() == KeyEvent.KEY_PRESSED) {
				toggleUnitCell();
			}
			return super.dispatchKeyEvent(e);
		}
	}

//	public BranchGroup putAtom(Vector3d v, Color3f c) {
//		BranchGroup a = (BranchGroup) createAtom(v, Utils3d.createApp(c), defaultValues.dotSize3d);
//		univers.addNotify(netRoot, a);
//		return a;
//	}
//
	public void toggleUnitCell() {
		if (showUnitCell) {
			univers.removeNotify(netRoot, unitCell);
		} else if (showUnitCell) {
			univers.addNotify(netRoot, unitCell);
		}
		if (!showDirect)
			toggleDirect();
		showUnitCell = !showUnitCell;
	}

	public void toggleDirect() {
		if (showDirect) {
			univers.removeNotify(netRoot, directRepere);
		} else {
			univers.addNotify(netRoot, directRepere);
		}
		showDirect = !showDirect;
	}

	static int atomid;

	private BranchGroup createAtom(Vector3d v, Appearance app, float dotSize3d) {
		BranchGroup bg = new BranchGroup();
		//bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		TransformGroup tg = Utils3d.getVectorTransformGroup(v.x, v.y, v.z, null);
		//tg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		++atomid;
		Utils3d.setParents(univers.renderer.createSphere("netroot:atom:sphere" + atomid, dotSize3d, 10, true, app), tg, bg);
		bg.setName("netroot:atom:" + atomid);
		return bg;
	}

	private void changeAtomApp(BranchGroup a, Appearance app) {
		Utils3d.getShapeChild(a).setAppearance(app);
	}

	public void setLambda(double val) {
		createNet(a, b, c, x, y, z);
	}
	
	public synchronized void createNet(Vector3d a, Vector3d b, Vector3d c, int x, int y, int z) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.x = x;
		this.y = y;
		this.z = z;
		
		directTR = null;
		scaling =  DefaultValues.scale * (model3d.isUnitSphere ? model3d.getLambda() : 1);

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
			univers.removeNotify((Group) orientationObject, netRoot);

		for (int i = 0; i < points.length; i++)
			for (int j = 0; j < points[i].length; j++)
				for (int k = 0; k < points[i][j].length; k++) {
					points[i][j][k] = null;
					dontdraw[i][j][k] = true;
				}

		netRoot = new BranchGroup();
		netRoot.setName("netroot:");
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
					createPoint(i, j, k, !developNet && v);
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
		if (!DefaultValues.javaJmol)
			netRoot.compile();
		univers.addNotify((Group) orientationObject, netRoot);
	}

	private void createPoint(int i, int j, int k, boolean visible) {
		Vector3d v = new Vector3d(); // (rotated) Cartesian, but same as abc
		v.scaleAdd(scaling * i, a, v); // (scaling is scale * lambda/a)
		v.scaleAdd(scaling * j, b, v);
		v.scaleAdd(scaling * k, c, v);
		BranchGroup atom = createAtom(v, defaultApp, defaultValues.dotSize3d);
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
		double h = DefaultValues.scale * zMax * c.getZ() + 1;
		Transform3D t3l = new Transform3D();
		t3l.rotZ(Math.PI / 2);
		TransformGroup tgl = univers.newWritableTransformGroup(t3l);
		Node txt = univers.creator.createFixedLegend("Reciprocal lattice", new Point3d(0, 0, h + .2), .1f,
				Utils3d.createApp(ColorConstants.blue), true);
		txt.setName("leg:reclatt");
		tgl.addChild(txt);
		txt = univers.creator.createFixedLegend("points" 
				+ (model3d.isUnitSphere ? 
				"*" + DefaultValues.strLambda 
				: ""), 
				new Point3d(0, 0, h), .1f, Utils3d.createApp(ColorConstants.blue), true);
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
		Node rep = univers.creator.createRepere(ColorConstants.cyan, ColorConstants.blue, 
				null, new String[] { "a*", "b*", "c*" }, .1f, .02f,
				defaultValues.dotSize, -defaultValues.dotSize, 
				(Vector3d) Utils3d.mul(a, 2*DefaultValues.scale),
				(Vector3d) Utils3d.mul(b, 2*DefaultValues.scale), 
				(Vector3d) Utils3d.mul(c, 2*DefaultValues.scale), false);
		rep.setName("rep:abc*");
		univers.addNotify(bb, rep);
		univers.addNotify(netRoot, bb);

		directRepere = new BranchGroup();
		directRepere.setName("directrepere");
		directRepere.setCapability(BranchGroup.ALLOW_DETACH);
		unitCell = new BranchGroup();
		unitCell.setName("unitCell");
		unitCell.setCapability(BranchGroup.ALLOW_DETACH);
		Vector3d[] r = Lattice.reciprocal(a, b, c);
		r[0].normalize();
		r[0].scale(.3 * DefaultValues.scale);
		r[1].normalize();
		r[1].scale(.3 * DefaultValues.scale);
		r[2].normalize();
		r[2].scale(.3 * DefaultValues.scale);
		rep = univers.creator.createRepere(ColorConstants.red, ColorConstants.red, 
				null, new String[] { "a", "b", "c" }, .15f, .02f, 
				defaultValues.dotSize, -defaultValues.dotSize, 
				r[0], r[1], r[2], false
				);
		rep.setName("repere:abc");
		univers.addNotify(directRepere, rep);
		if (showDirect) {
			netRoot.addChild(directRepere);
		}
	}
	
	public Transform3D getDirectTransform() {
		if (directTR != null)
			return directTR;
		if (!showDirect)
			netRoot.addChild(directRepere);
		directTR = univers.renderer.getTransform(directRepere);
		directTRInv = new Transform3D(directTR);
		directTRInv.invert();
		if (!showDirect) {
			netRoot.removeChild(directRepere);
		}
		
		return directTR;
	}
	public void createTranspBox() {
		Appearance app = new Appearance();
		// was app.setMaterial(new Material(white, blue, black, blue, 120.0f));
		app.setMaterial(new Material(ColorConstants.blue, ColorConstants.black, 
				ColorConstants.blue, ColorConstants.white, 120.0f));
		
		TransparencyAttributes transp = new TransparencyAttributes(TransparencyAttributes.NICEST, .90f);
		
		
		app.setTransparencyAttributes(transp);
		Transform3D t3d = new Transform3D();
		Matrix3d matrix = new Matrix3d();
		matrix.setColumn(0, (Vector3d) Utils3d.mul(a, x));
		matrix.setColumn(1, (Vector3d) Utils3d.mul(b, y));
		matrix.setColumn(2, (Vector3d) Utils3d.mul(c, z));
		t3d.set(matrix);
		Node box = univers.renderer.createBox("netroot:netbox", 2, 2, 2, app);
		Utils3d.setParents(box, univers.newWritableTransformGroup(t3d), netRoot);
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
