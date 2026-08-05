package org.epfl.diffractogram;

import javax.swing.JApplet;

import org.epfl.diffractogram.util.Lattice;

public class DefaultValues {

	// 2024.06.27 measured in Animator
	// Java/Jmol  ave 62.49 ms of 145
	// Java/J3d   ave 62.54 ms of 244		
	// Javascript ave 170.6 ms of 636


	/**
	 * if this is DiffractOgram2, so using Jmola and allowing goniometer switch
	 */
	public static boolean isDOG2;

	/**
	 * if Java (not JavaScript) and using Jmol; used only when creating the world to compile Net.netRoot
	 */
	public static /* not final */ boolean javaJmol = false;

	/**
	 * if Java or JavaScript and using Jmol
	 */
	public static boolean useJmol = /** @j2sNative true || */javaJmol;

	/**
	 * TRUE for a unit - not Ewald - sphere
	 * FALSE for original design
	 * 
	 */
	public static boolean isUnitSphere = true;
	
	/**
	 * TRUE to develop the reciprocal lattice as we go;
	 * FALSE for orginal functionality
	 */
	public static boolean developNet = true;

	/**
	 * TRUE here indicates rays should eminate directly from their reciprocal lattice point;
	 * FALSE is the original design with rays coming from the center of the RL.
	 */
	public static boolean directRays = true;

	
	/**
	 * TRUE to duplicate the S vector (g - e) within the reciprocal lattice
	 */
	public static boolean addRL_S = true;

    static void finalizeDefaults() {
		// forcing directRays if isUnitSphere
		if (isUnitSphere)
			directRays = true;
		useJmol = /** @j2sNative true || */javaJmol;
		showVersionDefaults();
	}

	public static void showVersionDefaults() {
		System.err.println("isUnitSphere = " + isUnitSphere);
		System.err.println("developNet = " + developNet);
		System.err.println("directRays = " + directRays);
		System.err.println("javaJmol = " + javaJmol);
		System.err.println("useJmol = " + useJmol);
	}
	
	public final static String UTF_Angstroms = "\u212b";
	public final static String UTF_Degrees = "\u00b0";
	

	@SuppressWarnings("unused")
	public static String strLambda = (true || useJmol ? "\u03bb" : "lambda");

	// constant values
	public final static double axisOffsets = .01;	
	public final static double ewaldSlop = 0.005;
	public final static float dotSizeNet = .025f;
	public final static double scale = 2;//5;
	public final static double maskDistFract = 1/2d;

	public static final float arrowWidth = .005f;

	// setable values
	
	public double param_zScreen = 4;
	public double param_wScreen = 10;
	public double param_hFlatScreen = 10;
	public double param_hCylScreen = 4;
	public double param_lambda = 1.5;//was 0.5;
	public int param_omega = 0;
	public int param_chi = 0;
	public int param_phi = 0;
	
	public int[] param_uvw = {0, 1, 0};
	public Lattice param_lattice = new Lattice(15, 15, 15, 90, 90, 90, param_uvw[0], param_uvw[1], param_uvw[2]);
			//new Lattice(5, 5, 5, 90, 90, 90, uvw[0], uvw[1], uvw[2]);
//	public int[] uvw = {0, 0, 1};
//	public Lattice lattice = new Lattice(4.75, 4.75, 12.89, 90, 90, 120, uvw[0], uvw[1], uvw[2]);
	public int param_crystalX = 3;
	public int param_crystalY = 3;
	public int param_crystalZ = 3;
	public int param_mu = 0;
	public int param_precession = 0;
	public int param_speed = 1;
	public int param_startAngle = 0;
	public int param_stopAngle = 360;
	
	public void parseParameters(JApplet applet) {
		
		param_lattice.a = parseDouble(applet.getParameter("a"), param_lattice.a);
		param_lattice.b = parseDouble(applet.getParameter("b"), param_lattice.b);
		param_lattice.c = parseDouble(applet.getParameter("c"), param_lattice.b);
		param_lattice.alpha = parseDouble(applet.getParameter("alpha"), param_lattice.alpha);
		param_lattice.beta = parseDouble(applet.getParameter("beta"), param_lattice.beta);
		param_lattice.gamma = parseDouble(applet.getParameter("gamma"), param_lattice.gamma);
		param_lattice = new Lattice(param_lattice.a, param_lattice.b, param_lattice.c, param_lattice.alpha, param_lattice.beta, param_lattice.gamma);

		Lattice rl = param_lattice.reciprocal();
		boolean rs = false;
		rs = rs && ((rl.a = parseDouble(applet.getParameter("astar"), -1)) > 0);
		rs = rs && ((rl.b = parseDouble(applet.getParameter("bstar"), -1)) > 0);
		rs = rs && ((rl.c = parseDouble(applet.getParameter("cstar"), -1)) > 0);
		rs = rs && ((rl.alpha = parseDouble(applet.getParameter("alphastar"), -1)) > 0);
		rs = rs && ((rl.beta = parseDouble(applet.getParameter("betastar"), -1)) > 0);
		rs = rs && ((rl.gamma = parseDouble(applet.getParameter("gammastar"), -1)) > 0);
		if (rs) {
			rl = new Lattice(rl.a, rl.b, rl.c, rl.alpha, rl.beta, rl.gamma);
			param_lattice = rl.reciprocal();
		}
		param_zScreen = parseDouble(applet.getParameter("distscreen"), param_zScreen);
		param_wScreen = parseDouble(applet.getParameter("wscreen"), param_wScreen);
		param_hFlatScreen = parseDouble(applet.getParameter("hflatscreen"), param_hFlatScreen);
		param_hCylScreen = parseDouble(applet.getParameter("hcylscreen"), param_hCylScreen);

		param_lambda = parseDouble(applet.getParameter("lambda"), param_lambda);
		param_crystalX = parseInt(applet.getParameter("h"), param_crystalX);
		param_crystalY = parseInt(applet.getParameter("k"), param_crystalY);
		param_crystalZ = parseInt(applet.getParameter("l"), param_crystalZ);
		param_omega = parseInt(applet.getParameter("omega"), param_omega);
		param_chi = parseInt(applet.getParameter("chi"), param_chi);
		param_phi = parseInt(applet.getParameter("phi"), param_phi);
		param_mu = parseInt(applet.getParameter("mu"), param_mu);
		param_precession = parseInt(applet.getParameter("precession"), param_precession);
		param_speed = parseInt(applet.getParameter("speed"), param_speed);
		param_startAngle = parseInt(applet.getParameter("startangle"), param_startAngle);
		param_stopAngle = parseInt(applet.getParameter("stopangle"), param_stopAngle);
		int u = parseInt(applet.getParameter("u"), Integer.MIN_VALUE);
		int v = parseInt(applet.getParameter("v"), Integer.MIN_VALUE);
		int w = parseInt(applet.getParameter("w"), Integer.MIN_VALUE);
		if (u != Integer.MIN_VALUE && v != Integer.MIN_VALUE && w != Integer.MIN_VALUE) {
			param_uvw[0] = u;
			param_uvw[1] = v;
			param_uvw[2] = w;			
		}		
		param_lattice.setOrientation(param_uvw[0], param_uvw[1], param_uvw[2]);
		
	}

	private static int parseInt(String p, int def) {
		try {
			return (p == null || p.length() == 0 ? def : Integer.parseInt(p));
		} catch (Exception e) {
			return def;
		}
	}

	private static double parseDouble(String p, double def) {
		try {
			return (p == null || p.length() == 0 ? def : Double.parseDouble(p));
		} catch (Exception e) {
			return def;
		}
	}

	
}
