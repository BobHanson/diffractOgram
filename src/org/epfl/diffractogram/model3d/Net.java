package org.epfl.diffractogram.model3d;

import java.awt.DefaultKeyboardFocusManager;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;

import org.epfl.diffractogram.DefaultValues;
import org.epfl.diffractogram.model3d.Model3d.Orientation;
import org.epfl.diffractogram.model3d.Model3d.Precession;
import org.epfl.diffractogram.util.Calc;
import org.epfl.diffractogram.util.Colors;
import org.epfl.diffractogram.util.Lattice;
import org.epfl.diffractogram.util.Utils3d;

/**
 * the Net class maintains lattice points in the displayed reciprocal lattice,
 * the goniometer head, and the floating label of the Net, all of which share
 * its orientation.
 * 
 */
public class Net extends BranchGroup {

	private static class Atom extends BranchGroup {

		static int atomid;

		boolean isSpecial;
		Point3d point;
		boolean isSelected;
		boolean isVisible;
		float intensity;

		private int id;

		Point3i hkl;

		Atom(int h, int k, int l, Vector3d v, boolean isSpecial, boolean isVisible, float intensity) {
			this.id = ++atomid;
			this.hkl = new Point3i(h, k, l);
			this.point = new Point3d(v);
			this.isSpecial = isSpecial;
			this.isVisible = isVisible;
			this.intensity = intensity;
			setName("netroot:atom:" + id);
			setCapability(BranchGroup.ALLOW_DETACH);
		}

		int getID() {
			return id;
		}
		
		public String toString() {
			return "Atom[" + hkl + " spec=" + isSpecial + " vis=" + isVisible + "]";
		}

	}

//	public UnitCell unitCell2;
	public TransformGroup orientationObject;
	public TransformGroup precessionObject;
	private BranchGroup netLabel;
	public BranchGroup netRoot;
	private static Appearance defaultApp, highlightApp, greenApp;
	private Vector3d aStar, bStar, cStar;
	public int hMax, kMax, lMax;
	public int hRange, kRange, lRange;
	BranchGroup directRepere;

	private BranchGroup unitCell;
	private boolean showDirect, showRLAxes;
	private Univers univers;

	double scaling;
	private Model3d model3d;
	private TransformGroup unitcellObject;
	public Lattice rl;

	private Transform3D directTR;
	public Transform3D directTRInv;
	private TransformGroup netBox;

	private Atom[][][] atoms;
	private List<Atom> selectedAtoms;

	public Net(Model3d model3d, DefaultValues defaultValues) {
		selectedAtoms = new ArrayList<>();
		rl = defaultValues.param_lattice.reciprocal();
		this.model3d = model3d;
		this.univers = model3d.univers;
		int hMax = defaultValues.param_crystalX;
		int kMax = defaultValues.param_crystalY;
		int lMax = defaultValues.param_crystalZ;
		this.setName("net");
		setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		if (defaultApp == null) {
			defaultApp = Utils3d.newAppearance("color:blue");
			defaultApp.setMaterial(new Material(Colors.blue, Colors.black, Colors.blue, Colors.white, 128));
		}
		if (highlightApp == null) {
			highlightApp = Utils3d.newAppearance("color:red");
			highlightApp.setMaterial(new Material(Colors.red, Colors.black, Colors.red, Colors.white, 128));
		}
		if (greenApp == null) {
			greenApp = Utils3d.newAppearance("color:green");
			greenApp.setMaterial(new Material(Colors.green, Colors.black, Colors.green, Colors.white, 128));
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

		createNet(rl.x, rl.y, rl.z, hMax, kMax, lMax);

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
				toggleReciprocalAxes();
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
	/**
	 * VK_F3
	 */
	public void toggleReciprocalAxes() {
		if (showRLAxes || !model3d.showReciprocalLattice) {
			univers.removeNotify(netRoot, unitCell);
		} else if (showRLAxes) {
			univers.addNotify(netRoot, unitCell);
		}
		if (!showDirect && model3d.showReciprocalLattice)
			toggleDirect();
		showRLAxes = model3d.showReciprocalLattice && !showRLAxes;
	}

	/**
	 * VK_F2
	 */
	public void toggleDirect() {
		if (showDirect || !model3d.showReciprocalLattice) {
			univers.removeNotify(netRoot, directRepere);
		} else {
			univers.addNotify(netRoot, directRepere);
		}
		showDirect = model3d.showReciprocalLattice && !showDirect;
	}

	private void changeAtomApp(Atom a, Appearance app) {
		if (a == null)
			return;
		Utils3d.getShapeChild(a).setAppearance(app);
	}

	public void setLambda(double val) {
		createNet(aStar, bStar, cStar, hMax, kMax, lMax);
	}

	public synchronized void createNet(Vector3d aStar, Vector3d bStar, Vector3d cStar, int hMax, int kMax, int lMax) {
		this.aStar = aStar;
		this.bStar = bStar;
		this.cStar = cStar;
		this.hMax = hMax;
		this.kMax = kMax;
		this.lMax = lMax;

		directTR = null;
		scaling = DefaultValues.scale * (DefaultValues.isUnitSphere ? model3d.getLambda() : 1);

		boolean trigonal = (Math.abs(aStar.length() - bStar.length()) < 0.001
				&& Math.round(bStar.angle(aStar) * 180 / Math.PI) == 60
				&& Math.round(bStar.angle(cStar) * 180 / Math.PI) == 90
				&& Math.round(cStar.angle(aStar) * 180 / Math.PI) == 90);

		// xMax, yMax, and zMax set the ranges for the point search
		// which for trigonal systems adds symmetry-equivalent extensions (and
		// duplicates!)
		hRange = hMax * (trigonal ? (2 * hMax + 1) : hMax);
		kRange = kMax * (trigonal ? (2 * kMax + 1) : kMax);
		lRange = lMax;

		atoms = new Atom[2 * hRange + 1][2 * kRange + 1][2 * lRange + 1];
		if (netRoot != null)
			univers.removeNotify((Group) orientationObject, netRoot);

		netRoot = new BranchGroup();
		netRoot.setName("netroot:");
		netRoot.setCapability(BranchGroup.ALLOW_DETACH);
		netRoot.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		netRoot.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

		boolean visible;
		for (int h = -hMax; h <= hMax; h++) {
			visible = (h == -hMax || h == hMax);
			for (int k = -kMax; k <= kMax; k++) {
				visible |= (k == -kMax || k == kMax);
				for (int l = -lMax; l <= lMax; l++) {
					visible |= (l == -lMax || l == lMax);
					createAtom(h, k, l, false, !DefaultValues.developNet && visible);
				}
			}
		}
		if (trigonal) {
			// leave out trigonal rotations
			// Q: What is this for? The range is too low?
			// but what about other orientations,
			// such as unique b instead of unique c?
			for (int h = -hMax; h <= hMax; h++) {
				for (int k = -kMax; k <= kMax; k++) {
					for (int l = -lMax; l <= lMax; l++) {
						int hk = h + k;
						if (getAtom(-hk, h, l) == null)
							createAtom(-hk, h, l, true, false);
						if (getAtom(k, -hk, l) == null)
							createAtom(k, -hk, l, true, false);
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

	private void createAtom(int h, int k, int l, boolean isSpecial, boolean visible) {
		Vector3d v = new Vector3d(); // (rotated) Cartesian, but same as abc
		v.scaleAdd(scaling * h, aStar, v); // (scaling is overallScale * lambda;)
		v.scaleAdd(scaling * k, bStar, v);
		v.scaleAdd(scaling * l, cStar, v);
		
		Atom atom = atoms[h + hRange][k + kRange][l + lRange] 
				= new Atom(h, k, l, v, isSpecial, visible, Calc.calcIntensity(aStar, bStar, cStar, h, k, l));
		Appearance app = isSpecial ? greenApp : defaultApp;
		float dotSize = DefaultValues.dotSize3d * (isSpecial ? 0.8f : 1);
		TransformGroup tg = Utils3d.getVectorTransformGroup(v.x, v.y, v.z, null);
		Utils3d.setParents(univers.renderer.createSphere("netroot:atom:sphere" + atom.getID(), dotSize, 10, true, app),
				tg, atom);
		if (visible && model3d.showReciprocalLattice)
			univers.addNotify(netRoot, atom);
	}

	public void createLegend() {
		if (!model3d.showReciprocalLattice) {
			univers.removeNotify(this, netLabel);
			return;
		}
		double h = DefaultValues.scale * lRange * cStar.getZ() + 1;
		Transform3D t3l = new Transform3D();
		t3l.rotZ(Math.PI / 2);
		TransformGroup tgl = univers.newWritableTransformGroup(t3l);
		Node txt = univers.creator.createFixedLegend("rl1", "Reciprocal lattice", new Point3d(0, 0, h + .2), .1f,
				Colors.appBlue, true);
		txt.setName("leg:reclatt");
		tgl.addChild(txt);
		txt = univers.creator.createFixedLegend("rl2",
				"points" + (DefaultValues.isUnitSphere ? "*" + DefaultValues.strLambda : ""), new Point3d(0, 0, h), .1f,
				Colors.appBlue, true);
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
		if (!model3d.showReciprocalLattice)
			return;
		unitCell = new BranchGroup();
		unitCell.setName("unitCell:");
		unitCell.setCapability(BranchGroup.ALLOW_DETACH);
		BranchGroup axes = univers.creator.createRepere("unitCell:", Colors.cyan, Colors.blue, null,
				new String[] { "a*", "b*", "c*" }, .1f, .02f, DefaultValues.axisOffsets, -DefaultValues.axisOffsets,
				(Vector3d) Utils3d.mul(aStar, 2 * DefaultValues.scale),
				(Vector3d) Utils3d.mul(bStar, 2 * DefaultValues.scale),
				(Vector3d) Utils3d.mul(cStar, 2 * DefaultValues.scale), false);
		if (model3d.showReciprocalLattice) {
			univers.addNotify(unitCell, axes);
			univers.addNotify(netRoot, unitCell);
		}

		directRepere = new BranchGroup();
		directRepere.setName("directrepere:");
		directRepere.setCapability(BranchGroup.ALLOW_DETACH);
		Vector3d[] r = Lattice.reciprocal(aStar, bStar, cStar);
		r[0].normalize();
		r[0].scale(.3 * DefaultValues.scale);
		r[1].normalize();
		r[1].scale(.3 * DefaultValues.scale);
		r[2].normalize();
		r[2].scale(.3 * DefaultValues.scale);
		axes = univers.creator.createRepere("directRepere:", Colors.red, Colors.red, null,
				new String[] { "a", "b", "c" }, .15f, .02f, DefaultValues.axisOffsets, -DefaultValues.axisOffsets, r[0],
				r[1], r[2], false);
		univers.addNotify(directRepere, axes);
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
		if (!showDirect)
			univers.removeNotify(netRoot, directRepere);
		return directTR;
	}

	public void createTranspBox() {
		if (!model3d.showReciprocalLattice || DefaultValues.developNet) {
			univers.removeNotify(netRoot, netBox);
			return;
		}
		Appearance app = new Appearance();
		// was app.setMaterial(new Material(white, blue, black, blue, 120.0f));
		app.setMaterial(new Material(Colors.blue, Colors.black, Colors.blue, Colors.white, 120.0f));
		TransparencyAttributes transp = new TransparencyAttributes(TransparencyAttributes.NICEST, .90f);
		app.setTransparencyAttributes(transp);
		Transform3D t3d = new Transform3D();
		Matrix3d matrix = new Matrix3d();
		matrix.setColumn(0, (Vector3d) Utils3d.mul(aStar, hMax));
		matrix.setColumn(1, (Vector3d) Utils3d.mul(bStar, lMax));
		matrix.setColumn(2, (Vector3d) Utils3d.mul(cStar, kMax));
		t3d.set(matrix);
		netBox = univers.newWritableTransformGroup(t3d);
		netBox.setName("netroot:netbox");
		Node box = univers.renderer.createBox("netBox", 2, 2, 2, app);
		Utils3d.setParents(box, netBox, netRoot);
	}

	public synchronized void setLattice(Lattice l) {
		createNet(l.x, l.y, l.z, hMax, kMax, lMax);
	}

	public synchronized void setCrystalSize(int x, int y, int z) {
		createNet(aStar, bStar, cStar, x, y, z);
	}

	public synchronized void highlight(Atom atom) {
		if (atom.isSelected)
			return;
		if (!atom.isVisible)
			univers.addNotify(netRoot, atom);
		changeAtomApp(atom, highlightApp);
		atom.isSelected = true;
		atom.isVisible = !atom.isSpecial;
		selectedAtoms.add(atom);
	}

	void clearSelectedAtoms() {
		for (int i = selectedAtoms.size(); --i >= 0;) {
			unhighlight(selectedAtoms.remove(i), false);
		}
	}

	public synchronized void unhighlight(Atom atom, boolean force) {
		if (force || atom.isSelected) {
			changeAtomApp(atom, defaultApp);
			if (force || !atom.isVisible)
				univers.removeNotify(netRoot, atom);
			atom.isSelected = false;
		}
	}

	public synchronized float intensity(int h, int k, int l) {
		return getAtom(h, k, l).intensity;
	}

	/**
	 * this may be null
	 * @param h
	 * @param k
	 * @param l
	 * @return atom for given hkl
	 */
	 public Atom getAtom(int h, int k, int l) {
		return atoms[h + hRange][k + kRange][l + lRange];
	}

	public void clearLattice() {
		for (int i = -hRange; i <= hRange; i++) {
			for (int j = -kRange; j <= kRange; j++) {
				for (int k = -lRange; k <= lRange; k++) {
					Atom atom = getAtom(i, j, k);
					if (atom != null)
						unhighlight(atom, true);
				}
			}
		}
	}

	public void doRaysOrLaue(Graphics mg, boolean adjustR, boolean isRay) {
		Vector3d vx = new Vector3d();
		Vector3d vy = new Vector3d();
		Vector3d vz = new Vector3d();
		Vector3d c = new Vector3d();
		Vector3d u = new Vector3d();
		vx.set(1, 0, 0);
		vy.set(0, 1, 0);
		vz.set(0, 0, 1);
		Precession precession = model3d.precession;
		Orientation orientation = model3d.orientation;
		precession.apply(vx);
		precession.apply(vy);
		precession.apply(vz);
		double screenDistance = model3d.p3d.y;
		double scaledRadius = model3d.virtualSphere.scaledRadius;
		c.set(0, screenDistance + (DefaultValues.directRays ? scaledRadius : 0), 0);
		double cn = c.dot(vy);
		Point3d unOrientedCenter = new Point3d(model3d.virtualSphere.center);
		precession.reverse(unOrientedCenter);
		orientation.reverse(unOrientedCenter);
		double rMask = Math.sin(precession.mu) * screenDistance;
		Point3d cMask = model3d.mask3d.center();
		Point3d pOrigin = Rays.o;
		Point3d cSphere = model3d.virtualSphere.center;
		Point3d pNet = new Point3d();
		Point3d pProj = new Point3d();
		clearSelectedAtoms();
		for (int h = -hRange; h <= hRange; h++) {
			for (int k = -kRange; k <= kRange; k++) {
				for (int l = -lRange; l <= lRange; l++) {
					if (h == 0 && k == 0 && l == 0)
						continue;
					Atom atom = getAtom(h, k, l);
					if (atom == null)
						continue;
					pNet.set(atom.point);
					model3d.tPrecOrient.transform(pNet);
					double ewaldDiff = 0;
					if (isRay) {
						// check for point at sphere
						ewaldDiff = unOrientedCenter.distance(atom.point) - scaledRadius;
						if (
								//ewaldDiff > 0 || 
								Math.abs(ewaldDiff) > DefaultValues.ewaldSlop)
							continue;
					}
					if (adjustR) {
						scaledRadius = -(pNet.x * pNet.x + pNet.y * pNet.y + pNet.z * pNet.z) / (2 * pNet.y);
						if (Double.isInfinite(scaledRadius) || Double.isNaN(scaledRadius) || scaledRadius <= 0d)
							continue;
					}
					pProj.set(pNet.x, pNet.y + scaledRadius, pNet.z);
					
					// project this point with precessed y(n) and screen center cn
					if (!model3d.p3d.projPoint(pProj, vy, cn))
						continue;
					u.set(pProj);
					if (isRay && model3d.p3d instanceof ProjScreen3d.Cylindrical) {
						// cylindric is much simple because no precession allowed
					} else {
						u.sub(c);
						u.set(u.dot(vx), u.dot(vy), u.dot(vz));
					}

					Point.Double p2d = model3d.p3d.proj3dTo2d(u);
					if (p2d == null)
						continue;

					boolean doProject2D = true;
					if (isRay) {
						Point3d pFrom = (DefaultValues.directRays ? null : pNet);
						Point3d pTo = (DefaultValues.directRays ? cSphere : pOrigin);
						if (model3d.mask && (Math.abs(pProj.distance(cMask) - rMask)) > .1) {
							pProj.scale(DefaultValues.maskDistFract);
							doProject2D = false;
						}
						if (DefaultValues.directRays)
							pProj.y -= scaledRadius;
						if (DefaultValues.isUnitSphere 
								&& model3d.persistent
								&& !atom.isSpecial) {
							// not showing all the rays -- just one -- when this is the unit sphere
							// because we have just one perpendicular M-projection visualization
							model3d.clearAllRays();
						}
						model3d.addImpactRay(cSphere, pFrom, pTo, pProj);
						if (DefaultValues.isUnitSphere)
							model3d.updateUnitSphere(h, k, l, atom.point, pNet);
						if (model3d.showReciprocalLattice) {
							highlight(atom);
						}
					}
					if (doProject2D)
						model3d.project2d(mg, p2d, h, k, l);
				}
			}
		}
	}

}
