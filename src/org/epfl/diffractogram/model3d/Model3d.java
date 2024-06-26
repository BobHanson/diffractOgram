package org.epfl.diffractogram.model3d;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Vector;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.epfl.diffractogram.DefaultValues;
import org.epfl.diffractogram.j3d.Java3DUniverse;
import org.epfl.diffractogram.jmol.JmolUniverse;
import org.epfl.diffractogram.util.Lattice;

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

	public Univers univers;
	public VirtualSphere virtualSphere;
	public ProjScreen3d p3d;
	public Net net;
	public ProjScreen projScreen;
	public boolean persistant = true;
	public Mask3d mask3d;
	public boolean mask = false;
	public Rays rays;
	private DefaultValues defaultValues;
	private double hCyl, hFlat;
	public Orientation orientation;
	public Precession precession;

	public Lattice lattice, reciprocal;

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
		virtualSphere = new VirtualSphere(univers, defaultValues, defaultValues.lambda);
		setFlatScreen();

		Lattice r = defaultValues.lattice.reciprocal();
		net = new Net(univers, orientation, precession, defaultValues, r.x, r.y, r.z, defaultValues.crystalX,
				defaultValues.crystalY, defaultValues.crystalZ);

		net.gonioHead.setY(virtualSphere.lambdaToRadius(defaultValues.lambda));

		mask3d = new Mask3d(univers, precession, defaultValues, p3d.y, 2, p3d.w, p3d.h);

		rays = new Rays(univers);

		univers.addNotify(null, virtualSphere);
		univers.addNotify(null, net);
		univers.addNotify(null, rays);

		// Debug.transparentScreen(Debug.root, new Vector3d(0,0,0), new Vector3d(1,0,0),
		// new Vector3d(0,0,1), new Vector3d(0,1,0), 2, 2, ColorConstants.green);
		// Debug.point(Debug.root, new Point3d(0, 2, 0), ColorConstants.black, .1);

	}

	public void setMask(boolean enabled) {
		if (!mask && enabled)
			univers.addNotify(null, mask3d);
		if (mask && !enabled)
			univers.removeNotify(null, mask3d);
		mask = enabled;
	}

	public synchronized void clearAllRays() {
		if (!persistant)
			projScreen.clearImage();
		rays.removeAllRays();
		for (int i = -net.xMax; i <= net.xMax; i++)
			for (int j = -net.yMax; j <= net.yMax; j++)
				for (int k = -net.zMax; k <= net.zMax; k++) {
					net.unHighlight(i, j, k);
				}
	}

	public synchronized void doRays(boolean adjustR) {
		clearAllRays();
		Vector3d n = new Vector3d(0, 1, 0);
		Vector3d e1 = new Vector3d(1, 0, 0);
		Vector3d e3 = new Vector3d(0, 0, 1);
		precession.apply(n);
		precession.apply(e1);
		precession.apply(e3);
		Vector3d c = new Vector3d(0, p3d.y, 0);
		double cn = c.dot(n);
		
		Point3d q = new Point3d();
		Point3d v = new Point3d();
		Vector3d u = new Vector3d();


		Transform3D tPrecOrient = new Transform3D();
		tPrecOrient.mul(precession.t3d, orientation.t3d);
		Point3d sReversed = new Point3d(virtualSphere.center);
		precession.reverse(sReversed);
		orientation.reverse(sReversed);
		double rMask = Math.sin(precession.mu) * p3d.y;
		Point3d cMask = mask3d.center();
		Graphics mg = projScreen.getGraphics();
		for (int h = -net.xMax; h <= net.xMax; h++)
			for (int k = -net.yMax; k <= net.yMax; k++)
				for (int l = -net.zMax; l <= net.zMax; l++) {
					if (h == 0 && k == 0 && l == 0)
						continue;
					Point3d p = net.points[h + net.xMax][k + net.yMax][l + net.zMax];

					if (p == null)
						continue;

					double r = virtualSphere.radius;
					double d = sReversed.distance(p) - r;
					if (d > 0 || Math.abs(d) > defaultValues.dotSize)
						continue;
					// TODO: ? if (Math.abs(d)>DefaultValues.dotSize) continue;

					q.set(p);
					tPrecOrient.transform(q);

					if (adjustR) {
						r = -(q.x * q.x + q.y * q.y + q.getZ() * q.getZ()) / (2 * q.y);
						if (Double.isInfinite(r) || Double.isNaN(r) || r <= 0d)
							continue;
					}

					v.set(q.x, q.y + r, q.getZ());
					if (!p3d.projPoint(v, n, cn))
						continue;

					if (p3d instanceof ProjScreen3d.Cylindric) {
						// cylindric is much simple because no precession allowed
						u.set(v);
					} else {
						u.sub(v, c);
						u.set(e1.dot(u), n.dot(u), e3.dot(u));
					}

					Point.Double p2d = p3d.proj3dTo2d(u);
					if (p2d == null)
						continue;

					if (mask && (d = Math.abs(v.distance(cMask) - rMask)) > .1) {
						v.scale(defaultValues.maskDistFract);
					} else {
						float intensity = net.intensity(h, k, l);
						projScreen.drawPoint(mg, p2d, intensity, (byte) h, (byte) k, (byte) l);
					}
					rays.addImpactRay(virtualSphere.center, q, v);
					net.highlight(h, k, l);
				}
		if (mg != null)
			mg.dispose();
	}

	public void doLaue() {
		Vector3d n = new Vector3d(0, 1, 0);
		Vector3d e1 = new Vector3d(1, 0, 0);
		Vector3d e3 = new Vector3d(0, 0, 1);
		precession.apply(n);
		precession.apply(e1);
		precession.apply(e3);
		Vector3d c = new Vector3d(0, p3d.y, 0);
		double cn = c.dot(n);
		
		Point3d q = new Point3d();
		Point3d v = new Point3d();
		Vector3d u = new Vector3d();

		Transform3D tPrecOrient = new Transform3D();
		tPrecOrient.mul(precession.t3d, orientation.t3d);
		Graphics mg = projScreen.getGraphics();
		for (int h = -net.xMax; h <= net.xMax; h++)
			for (int k = -net.yMax; k <= net.yMax; k++)
				for (int l = -net.zMax; l <= net.zMax; l++) {
					Point3d p = net.points[h + net.xMax][k + net.yMax][l + net.zMax];
					if (p == null)
						continue;
					q.set(p);
					tPrecOrient.transform(q);
					double r = -(q.getX() * q.getX() + q.getY() * q.getY() + q.getZ() * q.getZ()) / (2 * q.getY());
					if (Double.isInfinite(r) || Double.isNaN(r) || r <= 0d)
						continue;
					v.set(q.getX(), q.getY() + r, q.getZ());
					if (!p3d.projPoint(v, n, cn))
						continue;
					u.sub(v, c);
					u.set(e1.dot(u), n.dot(u), e3.dot(u));
					Point.Double p2d = p3d.proj3dTo2d(u);
					if (p2d == null)
						continue;
					float intensity = net.intensity(h, k, l);
					projScreen.drawPoint(mg, p2d, intensity, (byte) h, (byte) k, (byte) l);
				}
		mg.dispose();
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

	public void setFlatScreen() {
		setScreenType(new ProjScreen3d.Flat(univers, precession), p3d == null ? defaultValues.hFlatScreen : hFlat);
		projScreen.setImageSize(p3d.w, p3d.h, false);
	}

	public void setCylindricScreen() {
		setScreenType(new ProjScreen3d.Cylindric(univers, precession), p3d == null ? defaultValues.hCylScreen : hCyl);
		projScreen.setImageSize(p3d.y * Math.PI * 2, p3d.h, true);
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
			virtualSphere.setLambda(val);
			net.gonioHead.setY(virtualSphere.lambdaToRadius(val));
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

}