package org.epfl.diffractogram.model3d;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Vector;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import org.epfl.diffractogram.DefaultValues;
import org.epfl.diffractogram.j3d.Java3DUniverse;
import org.epfl.diffractogram.jmol.JmolUniverse;
import org.epfl.diffractogram.util.Lattice;
import org.epfl.diffractogram.util.Utils3d;

import model3d.ColorConstants;

/**
 * The Model3d class comprises all the univers.root shapes.
 * 
 * <pre>
 * 
 * These include:
 * 
 *   Net net
 *   
 *   Rays rays
 *   
 *   VirtualSphere s
 *   
 *   
 * 
 * It also maintains two specialized transformation-heavy classes:
 * 
 *    precessionClass: Used for maintaining angle mu and rotation alpha
 * 
 *    orientation: Used for maintaining omega, chi, and phi
 * 
 * </pre>
 */
public class Model3d {

	/**
	 * TRUE for a unit - not Ewald - sphere
	 * FALSE for original design
	 * 
	 */
	public final boolean isUnitSphere = true;
	
	/**
	 * TRUE to develop the reciprocal lattice as we go;
	 * FALSE for orginal functionality
	 */
	final static boolean developNet = true;

	/**
	 * TRUE here indicates rays should eminate directly from their reciprocal lattice point;
	 * FALSE is the original design with rays coming from the center of the RL.
	 */
	private final static boolean directRays = true;

	public Univers univers;
	public ProjScreen3d p3d;
	public Net net;
	public ProjScreen projScreen;
	public boolean persistent = true;
	public Orientation orientation;
	public Precession precession;
	public Mask3d mask3d;
	public Lattice lattice, reciprocal;
	
	protected VirtualSphere virtualSphere;

	private Rays rays;
	private GonioHead gonioHead;
	private DefaultValues defaultValues;
	private Transform3D tPrecOrient;
	private Transform3D tPrecOrientInv;
	private BranchGroup ucChild;
	private TransformGroup ucTG;
	
	private double hCyl, hFlat;
	private double lambda;
	private boolean mask;


	public Model3d(JPanel panel3d, DefaultValues defaultValues, ProjScreen projScreen) {
		this.defaultValues = defaultValues;

		lattice = new Lattice(defaultValues.lattice.a, defaultValues.lattice.b, defaultValues.lattice.c,
				defaultValues.lattice.alpha, defaultValues.lattice.beta, defaultValues.lattice.gamma);
		lattice.setOrientation(defaultValues.uvw[0], defaultValues.uvw[1], defaultValues.uvw[2]);
		
		reciprocal = lattice.reciprocal();

		univers = (DefaultValues.useJmol ? new JmolUniverse(panel3d) : new Java3DUniverse(panel3d));
		univers.rotX(-90);
		univers.rotY(-90);
		univers.setTopTransform();

		orientation = new Orientation();
		precession = new Precession();

		this.projScreen = projScreen;
		setLambda(defaultValues.lambda);
		virtualSphere = new VirtualSphere(this, defaultValues);
		setFlatScreen();

		createGonio();

		mask3d = new Mask3d(univers, precession, defaultValues, p3d.y, 2, p3d.w, p3d.h);

		rays = new Rays(this);

		univers.addNotify(null, virtualSphere);
		univers.addNotify(null, net);
		univers.addNotify(null, rays);
	}

	public void setMask(boolean enabled) {
		if (!mask && enabled)
			univers.addNotify(null, mask3d);
		if (mask && !enabled)
			univers.removeNotify(null, mask3d);
		mask = enabled;
	}

	private void createGonio() {
		net = new Net(this,defaultValues); 
		gonioHead = new GonioHead(this);
		univers.addNotify(net, gonioHead);			
		gonioHead.setY(virtualSphere.lambdaToRadius(lambda));
	}

	public synchronized void clearAllRays() {
		if (!persistent)
			clearImage();
		rays.removeAllRays(persistent);
		if (!developNet)
		for (int i = -net.xMax; i <= net.xMax; i++)
			for (int j = -net.yMax; j <= net.yMax; j++)
				for (int k = -net.zMax; k <= net.zMax; k++) {
					net.unHighlight(i, j, k);
				}
	}

	public void clearImage() {
		projScreen.clearImage();
		if (persistent)
			rays.removeAllRays(false);
	}
	
	/**
	 * 
	 * @param adjustR true only from lambda change
	 */
	public synchronized void doRays(boolean adjustR) {
		doRaysOrLaue(adjustR, true);
	}
	
	
	public void doLaue() {
		doRaysOrLaue(true, false);
	}

	
	/**
	 * 
	 * @param adjustR true only from lambda change
	 */
	private synchronized void doRaysOrLaue(boolean adjustR, boolean isRay) {
		clearAllRays();
		Vector3d vx = new Vector3d();
		Vector3d vy = new Vector3d();
		Vector3d vz = new Vector3d();
		Vector3d c = new Vector3d();
		Vector3d u = new Vector3d();
		vx.set(1, 0, 0);
		vy.set(0, 1, 0);
		vz.set(0, 0, 1);
		precession.apply(vx);
		precession.apply(vy);
		precession.apply(vz);
		double screenDistance = p3d.y;
		c.set(0, screenDistance + (directRays ? virtualSphere.scaledRadius : 0), 0);
		double cn = c.dot(vy);
		tPrecOrient = new Transform3D();
		tPrecOrient.mul(precession.t3d, orientation.t3d);
		tPrecOrientInv = new Transform3D(tPrecOrient);
		tPrecOrientInv.invert();
		Point3d sReversed = new Point3d(virtualSphere.center);
		precession.reverse(sReversed);
		orientation.reverse(sReversed);
		double rMask = Math.sin(precession.mu) * screenDistance;
		Point3d cMask = mask3d.center();
		Graphics mg = projScreen.getGraphics();
		Point3d pOrigin = Rays.o;
		Point3d cSphere = virtualSphere.center;
		Point3d pNet = new Point3d();
		Point3d pProj = new Point3d();
		double d;
		double scaledRadius = virtualSphere.scaledRadius;
		for (int h = -net.xMax; h <= net.xMax; h++) {
			for (int k = -net.yMax; k <= net.yMax; k++) {
				for (int l = -net.zMax; l <= net.zMax; l++) {
					if (h == 0 && k == 0 && l == 0)
						continue;
					Point3d p = net.points[h + net.xMax][k + net.yMax][l + net.zMax];
					if (p == null)
						continue;

					pNet.set(p);
					tPrecOrient.transform(pNet);

					if (isRay) {
						d = sReversed.distance(p) - scaledRadius;
						if (d > 0 || Math.abs(d) > defaultValues.dotSize)
							continue;
						// TODO: ? if (Math.abs(d)>DefaultValues.dotSize) continue;
					}
					if (adjustR) {
						scaledRadius = -(pNet.x * pNet.x + pNet.y * pNet.y + pNet.z * pNet.z) / (2 * pNet.y);
						if (Double.isInfinite(scaledRadius) || Double.isNaN(scaledRadius) || scaledRadius <= 0d)
							continue;
					}
					pProj.set(pNet.x, pNet.y + scaledRadius, pNet.z);

					// project this point with precessed y(n) and screen center cn
					if (!p3d.projPoint(pProj, vy, cn))
						continue;
					u.set(pProj);
					if (isRay && p3d instanceof ProjScreen3d.Cylindric) {
						// cylindric is much simple because no precession allowed
					} else {
						u.sub(c);
						u.set(u.dot(vx), u.dot(vy), u.dot(vz));
					}

					Point.Double p2d = p3d.proj3dTo2d(u);
					if (p2d == null)
						continue;

					if (isRay) {
						if (mask && (d = Math.abs(pProj.distance(cMask) - rMask)) > .1) {
							pProj.scale(defaultValues.maskDistFract);
						} else {
							float intensity = net.intensity(h, k, l);
							projScreen.drawPoint(mg, p2d, intensity, (byte) h, (byte) k, (byte) l);
						}
						if (directRays) {
							pProj.y -= virtualSphere.scaledRadius;
						}


						rays.addImpactRay(cSphere, pNet, directRays ? cSphere : pOrigin, pProj);
						if (isUnitSphere) {
							updateUnitSphere(h, k, l, p, pNet);
						}

						net.highlight(h, k, l);
					} else {
						float intensity = net.intensity(h, k, l);
						projScreen.drawPoint(mg, p2d, intensity, (byte) h, (byte) k, (byte) l);
					}
				}
			}
		}
		if (mg != null)
			mg.dispose();
	}

	public void setFlatScreen() {
		setScreenType(new ProjScreen3d.Flat(univers, precession), p3d == null ? defaultValues.hFlatScreen : hFlat);
		projScreen.setImageSize(p3d.w, p3d.h, false);
	}

	public void setCylindricScreen() {
		setScreenType(new ProjScreen3d.Cylindric(univers, precession), p3d == null ? defaultValues.hCylScreen : hCyl);
		projScreen.setImageSize(p3d.y * Math.PI * 2, p3d.h, true);
	}

	private void setScreenType(ProjScreen3d s, double h) {
		double w, y;
		if (p3d == null) {
			hCyl = defaultValues.hCylScreen;
			hFlat = defaultValues.hFlatScreen;
			w = defaultValues.wScreen;
			y = defaultValues.zScreen;
		} else {
			w = p3d.w;
			y = p3d.y;
			if (p3d instanceof ProjScreen3d.Flat)
				hFlat = p3d.h;
			else
				hCyl = p3d.h;
			univers.removeNotify(null, p3d);
		}
		p3d = s;
		p3d.setSize(w, h);
		p3d.setPos(y);
		univers.addNotify(null, p3d);
	}

	public void setScreenSize(double w, double h) {
		projScreen.setImageSize((p3d instanceof ProjScreen3d.Flat) ? w : (p3d.y * Math.PI * 2), h,
				p3d instanceof ProjScreen3d.Cylindric);
		p3d.setSize(w, h);
	}

	public void destroy() {
		if (univers != null)
			univers.cleanup();
	}

	public void complete() {
		doRays(false);
		univers.complete();
	}

	public void setLattice(float a, float b, float c, float alpha, float beta, float gamma) {
		lattice = new Lattice(a, b, c, alpha, beta, gamma);
		reciprocal = lattice.reciprocal();
		// next call is to setLatticeOrientation
	}

	public void setReciprocalLattice(float a, float b, float c, float alpha, float beta, float gamma) {
		reciprocal = new Lattice(a, b, c, alpha, beta, gamma);
		Lattice l = reciprocal.reciprocal();
		lattice = new Lattice(l.a, l.b, l.c, l.alpha, l.beta, l.gamma);
	}

	public void setLatticeOrientation(int u, int v, int w) {
		lattice.setOrientation(u, v, w);
		reciprocal = lattice.reciprocal();
		net.setLattice(reciprocal);
		updateAxes(null);
	}

	public void setDefaultParamters(DefaultValues defaultValues) {
		orientation.setOmega(defaultValues.omega);
		orientation.setChi(defaultValues.chi);
		orientation.setPhi(defaultValues.phi);
		precession.setAngle(defaultValues.mu);
		precession.setRotation(defaultValues.precession);
	}

	public boolean processActionCommand(String cmd, double val) {
		switch (cmd) {
		case "Lambda":
			setLambda(val);
			virtualSphere.setLambda(val);
			net.setLambda(val);
			gonioHead.setY(virtualSphere.lambdaToRadius(val));
			return true;
		case "Omega":
			orientation.setOmega(val);
			break;
		case "Chi":
			orientation.setChi(val);
			break;
		case "Phi":
			orientation.setPhi(val);
			break;
		case "Precession":
			precession.setRotation(val);
		}
		return false;
	}

	private void setLambda(double lambda) {
		this.lambda = lambda;
	}

	public void setPrecessionDefaults(DefaultValues defaultValues) {
		precession.setAngle(defaultValues.mu);
		mask3d.setR(Math.sin(precession.mu) * (p3d.y * defaultValues.maskDistFract));
	}

	/**
	 * Objects added  precession angle mu
	 * and rotation alpha along with a vector of PrecessionClass.PrecessionObject
	 * and its subclass PrecessionRotObject.
	 * 
	 * <pre>
	 * 
	 * Precessionobject objects include:
	 *      
	 *      model3d.projScreen3d.rotTg
	 *      model3d.net.precessionObject
	 *      model3d.mask3d.pTg
	 *
	 * PrecessionRotObject objects include:
	 *
	 *      model3d.mask3d.rotTg
	 *
	 * </pre>
	 */

	public static class Precession {

		public Transform3D t3d;

		public double mu, alpha;

		private Transform3D t3dx, t3dz, t3dy, t3dInv, t3dRot;
		private double angleX, angleZ;
		private Vector<TransformGroup> vpo, vpro;
		private final static boolean mathOnly = false;

		public Precession() {
			t3dx = new Transform3D();
			t3dz = new Transform3D();
			t3d = new Transform3D();
			t3dInv = new Transform3D();
			t3dy = new Transform3D();
			t3dRot = new Transform3D();
			vpo = new Vector<>(10, 10);
			vpro = new Vector<>(10, 10);
		}

		public TransformGroup addPrecessionObject(TransformGroup o) {
			vpo.add(o);
			return o;
		}

		public TransformGroup addPrecessionRotObject(TransformGroup o) {
			vpro.add(o);
			return o;
		}

		private void updateObjects() {
			angleX = mu * Math.cos(alpha);
			angleZ = mu * Math.sin(alpha);
			t3dx.rotX(angleX);
			t3dz.rotZ(angleZ);
			t3dy.rotY(-alpha);
			t3d.mul(t3dz, t3dx);
			t3dx.rotX(-angleX);
			t3dz.rotZ(-angleZ);
			t3dInv.mul(t3dx, t3dz);
			t3dRot.mul(t3d, t3dy);
			if (!mathOnly) {
				for (int i = 0; i < vpo.size(); i++) {
					vpo.get(i).setTransform(t3d);
				}
				for (int i = 0; i < vpro.size(); i++) {
					vpro.get(i).setTransform(t3dRot);
				}
			}
		}

		public void setAngle(double mu) {
			this.mu = Math.PI * mu / 180;
			updateObjects();
		}

		public void setRotation(double alpha) {
			this.alpha = Math.PI * alpha / 180;
			updateObjects();
		}

		public void apply(Point3d p) {
			if (alpha != 0 || mu != 0) {
				t3d.transform(p);
			}
		}

		public void apply(Vector3d v) {
			if (alpha != 0 || mu != 0) {
				t3d.transform(v);
			}
		}

		public void reverse(Point3d p) {
			if (alpha != 0 || mu != 0) {
				t3dInv.transform(p);
			}
		}

		public void reverse(Vector3d v) {
			if (alpha != 0 || mu != 0) {
				t3dInv.transform(v);
			}
		}

		public void applyRot(Point3d p) {
			if (alpha != 0 || mu != 0) {
				t3dRot.transform(p);
			}
		}

		public void applyRot(Vector3d v) {
			if (alpha != 0 || mu != 0) {
				t3dRot.transform(v);
			}
		}

	}

	/**
	 * All objects added to this class are transformed by t3d, but the goniometer
	 * ring orientation is only set by omega, so it is made to be an omega-only
	 * object.
	 * 
	 * <pre>
	 * 
	 * OrientationObjects include:
	 * 
	 * model3d.net.orientationObject 
	 * model3d.net.gonioHead.orientationGonio
	 * 
	 * </pre>
	 */
	public static class Orientation {

		private Transform3D t3d;
		public double omega, chi, phi;

		private Vector<TransformGroup> v, vOmegaOnly;

		private Transform3D t3dOmega, t3dChi, t3dPhi, t3dOmegaChi, t3dInv;
		private final static boolean mathOnly = false;

		public Orientation() {
			t3d = new Transform3D();
			t3dInv = new Transform3D();
			t3dOmega = new Transform3D();
			t3dChi = new Transform3D();
			t3dOmegaChi = new Transform3D();
			t3dPhi = new Transform3D();
			
			v = new Vector<>(10, 10);
			vOmegaOnly = new Vector<>(10, 10);
		}

		public TransformGroup addOrientationObject(TransformGroup o) {
			v.add(o);
			return o;
		}

		public TransformGroup addOrientationOmegaOnly(TransformGroup o) {
			vOmegaOnly.add(o);
			return o;
		}

		private void updateAll() {
			if (mathOnly)
				return;
			for (int i = 0; i < v.size(); i++) {
				v.get(i).setTransform(t3d);
			}
		}

		private void updateOmegaOnly() {
			if (mathOnly)
				return;
			for (int i = 0; i < vOmegaOnly.size(); i++) {
				vOmegaOnly.get(i).setTransform(t3dOmega);
			}
		}

		public void setOmega(double angle) {
			omega = Math.PI * angle / 180;
			t3dOmega.rotZ(omega);
			t3dOmegaChi.mul(t3dOmega, t3dChi);
			t3d.mul(t3dOmegaChi, t3dPhi);
			t3dInv.invert(t3d);
			updateAll();
			updateOmegaOnly();
		}

		public void setChi(double angle) {
			chi = Math.PI * angle / 180;
			t3dChi.rotY(chi);
			t3dOmegaChi.mul(t3dOmega, t3dChi);
			t3d.mul(t3dOmegaChi, t3dPhi);
			t3dInv.invert(t3d);
			updateAll();
		}

		public void setPhi(double angle) {
			phi = Math.PI * angle / 180;
			t3dPhi.rotZ(phi);
			t3d.mul(t3dOmegaChi, t3dPhi);
			t3dInv.invert(t3d);
			updateAll();
		}

		public void apply(Point3d p) {
			t3d.transform(p);
		}

		public void apply(Vector3d v) {
			t3d.transform(v);
		}

		public void reverse(Point3d p) {
			t3dInv.transform(p);
		}

		public void reverse(Vector3d v) {
			t3dInv.transform(v);
		}

		public void applyOmega(Vector3d v) {
			t3dOmega.transform(v);
		}

		public void applyChi(Vector3d v) {
			t3dChi.transform(v);
		}

		public void applyPhi(Vector3d v) {
			t3dPhi.transform(v);
		}

	}

	/**
	 * The Goniometer Head, also the unitSphere contents if that is present
	 *
	 */
	public static class GonioHead extends BranchGroup {
		private TransformGroup orientationGonio;
		private TransformGroup orientationGonioRing;
		private Transform3D t3d;
		private TransformGroup tg;
	
		protected GonioHead(Model3d model3d) {
			setName("goinohead");
			orientationGonio = model3d.orientation.addOrientationObject(model3d.univers.newWritableTransformGroup(null));
			orientationGonioRing = model3d.orientation.addOrientationOmegaOnly(model3d.univers.newWritableTransformGroup(null));
			t3d = new Transform3D();
			tg = model3d.univers.newWritableTransformGroup(t3d);
			Utils3d.setParents((Node) orientationGonio, tg, this);
			Utils3d.setParents((Node) orientationGonioRing, tg, this);
			buildGadjet(model3d, orientationGonio, orientationGonioRing);
		}
	
		protected void setY(double y) {
			t3d.set(new Vector3d(0, -y, 0));
			tg.setTransform(t3d);
		}
	
		private static void buildGadjet(Model3d model3d, TransformGroup gonio, TransformGroup gonioRing) {
			if (model3d.isUnitSphere) {
				double scale = model3d.virtualSphere.scaledRadius;
				Transform3D t = new Transform3D();
				t.setScale(scale);
				TransformGroup tg = new TransformGroup(t);
				tg.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
				tg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
				model3d.updateAxes(tg);
				Utils3d.setParents(tg, (Group) gonio);
			} else {
				Node ring = model3d.univers.renderer.createTorus("ring", .05, .6, 10, 50, Utils3d.createApp(ColorConstants.yellow));
				Node sample = model3d.univers.renderer.createBox("stage", 0.2, 0.2, 0.1, Utils3d.createApp(ColorConstants.blue));
				Node pin = model3d.univers.creator.createCylinder(model3d.univers, "pin", new Point3d(),
						new Point3d(0, 0, -.4), .02, Utils3d.createApp(ColorConstants.orange), 5);
				// omega ring
				Utils3d.setParents(ring, gonioRing);
				// sample crystal
				Utils3d.setParents(sample, Utils3d.getVectorTransformGroup(0, 0, -.4, null), (Group) gonio);
				// sample pin
				Utils3d.setParents(pin, (Group) gonio);
			}
		}
		
	}

	public double getLambda() {
		return lambda;
	}
	
	public void updateAxes(TransformGroup tg) {
		if (tg == null)
			tg = ucTG;
		else
			ucTG = tg;
		tg.removeChild(ucChild);
		BranchGroup n = univers.creator.createRepere(ColorConstants.red, 
				ColorConstants.green, null, 
				new String[] { "a^", "b^", "c^" },
				.055f, .01f, 0, 0, 
				(Vector3d)transformLatticeV(new Vector3d(1/lattice.a, 0, 0), 1),
				(Vector3d)transformLatticeV(new Vector3d(0, 1/lattice.b, 0), 1),
				(Vector3d)transformLatticeV(new Vector3d(0, 0, 1/lattice.c), 1), true);
		n.setName("unitcell:abc ");
		n.setCapability(BranchGroup.ALLOW_DETACH);
		ucChild = n;
		tg.addChild(n);
	}

	public Tuple3d transformLatticeV(Tuple3d v, double scale) {
		lattice.transform(v);
		if (scale <= 0) {
			double len = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
			v.scale(1 / len);
			if (scale < 0)
				v.scale(-scale);
		} else {
			v.scale(scale);
		}
		return v;
	}

	public void inverseTransformLatticeV(Tuple3d v) {
		lattice.invertTransform(v);
	}

	private BranchGroup m, ms0, ma, mb, mc;
	
	void updateUnitSphere(int h, int k, int l, Point3d pN, Point3d pNet) {

		// reverse S0
		double r = virtualSphere.scaledRadius;
		Point3d p0 = new Point3d(0, -r, 0);
		Point3d pSo = new Point3d(pNet);
		pSo.add(p0);
		setMrays(pNet, pSo, p0);

//		Point3d pNetTr = new Point3d(pNet);
//		tPrecOrientInv.transform(pNetTr); // now pN
//		reciprocal.inverse.transform(pNetTr);
//		pNetTr.scale(1/net.scaling);// now this is {h k l} YES
		Point3d pNetTr = new Point3d(h, k, l);
		reciprocal.rotate(pNetTr); // now (lambda/a, -lambda/b, 3 lambda/c)

		// The three direct vectors

		Vector3d va = (Vector3d) transformLatticeV(new Vector3d(1, 0, 0), 0);
		Vector3d vb = (Vector3d) transformLatticeV(new Vector3d(0, 1, 0), 0);
		Vector3d vc = (Vector3d) transformLatticeV(new Vector3d(0, 0, 1), 0);

		// M dot a, M dot b, M dot c

		double mDotA = Utils3d.dot(va, pNetTr);
		double mDotB = Utils3d.dot(vb, pNetTr);
		double mDotC = Utils3d.dot(vc, pNetTr);

		// these are now h*lamba/a, k*lambda/b, l*lambda/c

		// back to the net scaling -- lambda * DefaultValues.scale

		va.scale(mDotA * net.scaling);
		vb.scale(mDotB * net.scaling);
		vc.scale(mDotC * net.scaling);

		// these are the projection points we need to along the axes.

		tPrecOrient.transform(va);
		tPrecOrient.transform(vb);
		tPrecOrient.transform(vc);

		// now in the same basis as rays

		if (Math.abs(mDotA) > 0.05) {
			p0.set(va);
			p0.y -= r;
			if (p0.distance(pSo) > 0.05) {
				ma = univers.creator.createNamedVector(" " + h, pSo, p0, p0, 0.2f, 0.06f, ColorConstants.red,
						ColorConstants.red);
				ma.setCapability(BranchGroup.ALLOW_DETACH);
				univers.addNotify(rays, ma);
			}
		}
		if (Math.abs(mDotB) > 0.05) {
			p0.set(vb);
			p0.y -= r;
			if (p0.distance(pSo) > 0.05) {
				mb = univers.creator.createNamedVector(" " + k, pSo, p0, p0, 0.2f, 0.06f, ColorConstants.red,
						ColorConstants.red);
				mb.setCapability(BranchGroup.ALLOW_DETACH);
				univers.addNotify(rays, mb);
			}
		}
		if (Math.abs(mDotC) > 0.05) {
			p0.set(vc);
			p0.y -= r;
			if (p0.distance(pSo) > 0.05) {
				mc = univers.creator.createNamedVector(" " + l, pSo, p0, p0, 0.2f, 0.06f, ColorConstants.red,
						ColorConstants.red);
				mc.setCapability(BranchGroup.ALLOW_DETACH);
				univers.addNotify(rays, mc);
			}
		}
	}

	private void setMrays(Point3d pNet, Point3d pSo, Point3d p0) {
		if (m != null) {
			rays.removeChild(m);
			rays.removeChild(ms0);
			if (ma != null) {
				rays.removeChild(ma);
			}
			if (mb != null) {
				rays.removeChild(mb);
			}
			if (mc != null) {
				rays.removeChild(mc);
			}
		}
		Appearance yellow = Utils3d.createApp(ColorConstants.yellow);
		Appearance red = Utils3d.createApp(ColorConstants.red);
		m = univers.creator.createCylinder(univers, "M", p0, pSo, .02, red, 4);
		univers.addNotify(rays, m);
		ms0 = univers.creator.createCylinder(univers, "-So", pNet, pSo, .02, yellow, 4);
		univers.addNotify(rays, ms0);
	}


}