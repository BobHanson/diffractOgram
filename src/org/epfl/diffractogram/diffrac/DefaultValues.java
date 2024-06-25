package org.epfl.diffractogram.diffrac;

import javax.swing.JApplet;

public class DefaultValues {

	public static /* not final */ boolean javaJmol = false;
	public static boolean useJmol = /** @j2sNative true || */javaJmol;

	static {
		System.err.println("DefaultValues.javaJmol=" + javaJmol);
	}
	
	public final static String UTF_Angstroms = "\u212b";
	public final static String UTF_Degrees = "\u00b0";
	
	public static String strLambda = (useJmol ? "\u03bb" : "lambda");

	public double zScreen = 4;
	public double wScreen = 10;
	public double hFlatScreen = 10;
	public double hCylScreen = 4;
	public double lambda = 0.5;
	public int omega = 0;
	public int chi = 0;
	public int phi = 0;
	
	public int[] uvw = {0, 1, 0};
	public Lattice lattice = new Lattice(5, 5, 5, 90, 90, 90, uvw[0], uvw[1], uvw[2]);
//	public int[] uvw = {0, 0, 1};
//	public Lattice lattice = new Lattice(4.75, 4.75, 12.89, 90, 90, 120, uvw[0], uvw[1], uvw[2]);
	public int crystalX = 3;
	public int crystalY = 3;
	public int crystalZ = 3;
	public double scale = 2;
	public float dotSize = .01f;
	public float dotSize3d = .05f;
	public int mu = 0;
	public int precession = 0;
	public double maskDistFract = 1/2d;
	public int speed = 1;
	public int startAngle = 0;
	public int stopAngle = 360;
	
	public void parseParameters(JApplet applet) {
		try {lattice.a = Double.parseDouble(applet.getParameter("a"));}catch(Exception e) {}
		try {lattice.b = Double.parseDouble(applet.getParameter("b"));}catch(Exception e) {}
		try {lattice.c = Double.parseDouble(applet.getParameter("c"));}catch(Exception e) {}
		try {lattice.alpha = Double.parseDouble(applet.getParameter("alpha"));}catch(Exception e) {}
		try {lattice.beta = Double.parseDouble(applet.getParameter("beta"));}catch(Exception e) {}
		try {lattice.gamma = Double.parseDouble(applet.getParameter("gamma"));}catch(Exception e) {}
		lattice = new Lattice(lattice.a, lattice.b, lattice.c, lattice.alpha, lattice.beta, lattice.gamma);

		Lattice rl = lattice.reciprocal();
		boolean rs=false;
		try {rl.a = Double.parseDouble(applet.getParameter("astar"));rs=true;}catch(Exception e) {}
		try {rl.b = Double.parseDouble(applet.getParameter("bstar"));rs=true;}catch(Exception e) {}
		try {rl.c = Double.parseDouble(applet.getParameter("cstar"));rs=true;}catch(Exception e) {}
		try {rl.alpha = Double.parseDouble(applet.getParameter("alphastar"));rs=true;}catch(Exception e) {}
		try {rl.beta = Double.parseDouble(applet.getParameter("betastar"));rs=true;}catch(Exception e) {}
		try {rl.gamma = Double.parseDouble(applet.getParameter("gammastar"));rs=true;}catch(Exception e) {}
		if (rs) {
			rl = new Lattice(rl.a, rl.b, rl.c, rl.alpha, rl.beta, rl.gamma);
			lattice = rl.reciprocal();
		}
		
		try {crystalX = Integer.parseInt(applet.getParameter("h"));}catch(Exception e) {}
		try {crystalY = Integer.parseInt(applet.getParameter("k"));}catch(Exception e) {}
		try {crystalZ = Integer.parseInt(applet.getParameter("l"));}catch(Exception e) {}

		try {zScreen = Double.parseDouble(applet.getParameter("distscreen"));}catch(Exception e) {}
		try {wScreen = Double.parseDouble(applet.getParameter("wscreen"));}catch(Exception e) {}
		try {hFlatScreen = Double.parseDouble(applet.getParameter("hflatscreen"));}catch(Exception e) {}
		try {hCylScreen = Double.parseDouble(applet.getParameter("hcylscreen"));}catch(Exception e) {}

		try {omega = Integer.parseInt(applet.getParameter("omega"));}catch(Exception e) {}
		try {chi = Integer.parseInt(applet.getParameter("chi"));}catch(Exception e) {}
		try {phi = Integer.parseInt(applet.getParameter("phi"));}catch(Exception e) {}
		try {lambda = Double.parseDouble(applet.getParameter("lambda"));}catch(Exception e) {}
		try {mu = Integer.parseInt(applet.getParameter("mu"));}catch(Exception e) {}
		try {precession = Integer.parseInt(applet.getParameter("precession"));}catch(Exception e) {}
		
		try {
			int u = Integer.parseInt(applet.getParameter("u"));
			int v = Integer.parseInt(applet.getParameter("v"));
			int w = Integer.parseInt(applet.getParameter("w"));
			uvw[0] = u;
			uvw[1] = v;
			uvw[2] = w;
		}catch(Exception e) {}

		try {speed = Integer.parseInt(applet.getParameter("speed"));}catch(Exception e) {}
		try {startAngle = Integer.parseInt(applet.getParameter("startangle"));}catch(Exception e) {}
		try {stopAngle = Integer.parseInt(applet.getParameter("stopangle"));}catch(Exception e) {}
		
		lattice.setOrientation(uvw[0], uvw[1], uvw[2]);
		
/*		

		mask=on|off
		limited=on|off

		animate=omega|chi|phi|seq|debye|lambda|precession
		drawlaue=on|off
		screen=flat|cylindric
		persistant=off|on		
*/		
	}
	
}
