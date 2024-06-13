package org.epfl.diffractogram.model3d;

import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import org.epfl.diffractogram.diffrac.DefaultValues;
import org.epfl.diffractogram.diffrac.Lattice;
import org.epfl.diffractogram.transformations.OrientationClass;
import org.epfl.diffractogram.transformations.OrientationClass.OrientationObject;
import org.epfl.diffractogram.transformations.PrecessionClass;
import org.epfl.diffractogram.transformations.PrecessionClass.PrecessionObject;
import org.epfl.diffractogram.util.Java3dUtil.Appearance;
import org.epfl.diffractogram.util.Java3dUtil.Box;
import org.epfl.diffractogram.util.Java3dUtil.BranchGroup;
import org.epfl.diffractogram.util.Java3dUtil.Color3f;
import org.epfl.diffractogram.util.Java3dUtil.Material;
import org.epfl.diffractogram.util.Java3dUtil.Matrix3d;
import org.epfl.diffractogram.util.Java3dUtil.Point3d;
import org.epfl.diffractogram.util.Java3dUtil.Shape3D;
import org.epfl.diffractogram.util.Java3dUtil.Sphere;
import org.epfl.diffractogram.util.Java3dUtil.Transform3D;
import org.epfl.diffractogram.util.Java3dUtil.TransformGroup;
import org.epfl.diffractogram.util.Java3dUtil.TransparencyAttributes;
import org.epfl.diffractogram.util.Java3dUtil.Vector3d;
import org.epfl.diffractogram.util.Java3dUtil.Torus;


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
	private boolean directShowed=false;
	private DefaultValues defaultValues;
	
	public Net(OrientationClass orientationClass, PrecessionClass precessionClass, DefaultValues defaultValues, Vector3d a, Vector3d b, Vector3d c, int x, int y, int z) {
		this.defaultValues = defaultValues;
		setCapabilityTo(TransformGroup.ALLOW_CHILDREN_EXTEND);
		setCapabilityTo(TransformGroup.ALLOW_CHILDREN_WRITE);
		if (defaultApp==null) {
			defaultApp = new Appearance();
			defaultApp.setMaterial(new Material(blue, black, blue, white, 128));
		}
		if (redApp==null) {
			redApp = new Appearance();
			redApp.setMaterial(new Material(red, black, red, white, 128));
		}
		if (greenApp==null) {
			greenApp = new Appearance();
			greenApp.setMaterial(new Material(green, black, green, white, 128));
		}
		
		orientationObject = orientationClass.new OrientationObject();
		orientationObject.setCapabilityTo(TransformGroup.ALLOW_CHILDREN_EXTEND);
		orientationObject.setCapabilityTo(TransformGroup.ALLOW_CHILDREN_WRITE);
		
		precessionObject = precessionClass.new PrecessionObject();
		precessionObject.addChild(orientationObject);
	
		createNet(a, b, c, x, y, z);

		addChild(precessionObject);
		addChild(gonioHead = new GonioHead(orientationClass));

		KeyboardFocusManager kbfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    kbfm.addKeyEventDispatcher(new MyKeyboardManager());
	}

	private void setCapabilityTo(int c) {
		super.setCapability(c);		
	}

	public class MyKeyboardManager extends DefaultKeyboardFocusManager {
		public boolean dispatchKeyEvent(KeyEvent e) {
			if (e.getKeyCode()==KeyEvent.VK_F1 && e.getID()==KeyEvent.KEY_PRESSED) {
				if (directShowed) {
					netRoot.removeChild(directRepere);
				}
				else {
					netRoot.addChild(directRepere);
				}
				directShowed = !directShowed;
			}
			return super.dispatchKeyEvent(e);
		}
	}		
	
	public BranchGroup putAtom(Vector3d v, Color3f c) {
		BranchGroup a = (BranchGroup)createAtom(v, Utils3d.createApp(c), defaultValues.dotSize3d);
		netRoot.addChild(a);
		return a;
	}
	
	private static BranchGroup createAtom(Vector3d v, Appearance app, float dotSize3d) {
		Transform3D t = new Transform3D();
		t.set(v);
		TransformGroup tg = new TransformGroup(t);
		tg.setCapabilityTo(TransformGroup.ALLOW_CHILDREN_READ);
		Sphere s = new Sphere(dotSize3d, Sphere.GENERATE_NORMALS, 10, app);
		s.getShape().setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		tg.addChild(s);
		BranchGroup bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.addChild(tg);
		return bg;
	}
	
	private static BranchGroup createAtom(Vector3d v, float dotSize3d) {
		return createAtom(v, defaultApp, dotSize3d);
	}
	private void changeAtomApp(BranchGroup a, Appearance app) {
		((Sphere)((TransformGroup)a.getChild(0)).getChild(0)).setAppearance(app);
	}
	
	public synchronized float calcIntensity(int h, int k, int l) {
		Vector3d v = new Vector3d();
		v.scaleAdd(h, Utils3d.norm(a), v);
		v.scaleAdd(k, Utils3d.norm(b), v);
		v.scaleAdd(l, Utils3d.norm(c), v);
		float r = (float)v.length();
		//return 1f/(4f*r*r)+.03f;
		//return 1f/(4f*(float)Math.pow(r, 1.4))+.03f;

		//float i = 2f*(float)Math.exp(-r)+.03f; 
		//float i = 10f*(float)Math.exp(-.3*r)+.3f;
		float i = 8f*(float)Math.exp(-.5*r)+.2f;
		
		if (i < .4f) i=.4f;
		if (i > 1) i=1;
		
		return i;
	}
	
	
	public synchronized void createNet(Vector3d a, Vector3d b, Vector3d c, int x, int y, int z) {
		this.a=a; this.b=b; this.c=c; this.x=x; this.y=y; this.z=z; 
		
		boolean special = false;
		if (Math.abs(a.length()-b.length())<0.001 && Math.round(b.angle(a)*180/Math.PI)==60 && Math.round(b.angle(c)*180/Math.PI)==90 && Math.round(c.angle(a)*180/Math.PI)==90)
			special = true;
		
		xMax = x*(special?(2*x+1):x);
		yMax = y*(special?(2*y+1):y);
		zMax = z;

		points = new Point3d[2*xMax+1][2*yMax+1][2*zMax+1];
		atoms = new BranchGroup[2*xMax+1][2*yMax+1][2*zMax+1];
		isSelected = new boolean[2*xMax+1][2*yMax+1][2*zMax+1];
		dontdraw = new boolean[2*xMax+1][2*yMax+1][2*zMax+1];
		intensity = new float[2*xMax+1][2*yMax+1][2*zMax+1];
		isProjected = new boolean[2*xMax+1][2*yMax+1][2*zMax+1];
		orientationObject.removeChild(netRoot);
		
		for (int i=0; i<points.length; i++)
			for (int j=0; j<points[i].length; j++)
				for (int k=0; k<points[i][j].length; k++) {
					points[i][j][k] = null;
					dontdraw[i][j][k] = true;
				}
		
		netRoot = new BranchGroup();
		netRoot.setCapability(BranchGroup.ALLOW_DETACH);
		netRoot.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		netRoot.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		
		for (int i=-x; i<=x; i++) {
			for (int j=-y; j<=y; j++) {
				for (int k=-z; k<=z; k++) {
					//dontdraw[i+xMax][j+yMax][k+zMax] = true;
					//dontdraw[i+xMax][j+yMax][k+zMax] = ((i!=-x && i!=x) || (j!=-y && j!=y) || (k!=-z && k!=z));
					//dontdraw[i+xMax][j+yMax][k+zMax] = (i!=-x && i!=x && j!=-y && j!=y && k!=-z && k!=z);
					//dontdraw[i+xMax][j+yMax][k+zMax] = !(i==-x && i!=x && j!=-y && j!=y && k!=-z && k!=z);
					//dontdraw[i+xMax][j+yMax][k+zMax] = (j!=-y && j!=y && k!=-z && k!=z);
					boolean v = !(i!=-x && i!=x && j!=-y && j!=y && k!=-z && k!=z); 
					createPoint(i, j, k, v); 
					if (special) {
						createPoint(-(i+j), i, k, false); 
						createPoint(j, -(i+j), k, false); 
					}
				}}}
		
		createLegend();
		createRepere();
		createTranspBox();
		netRoot.compile();
		orientationObject.addChild(netRoot);
	}
	
	private void createPoint(int i, int j, int k, boolean visible) {
		//System.out.println((i+xMax)+" "+(j+yMax)+" "+(k+zMax)+" "+(2*xMax+1)+" "+(2*yMax+1)+" "+(2*zMax+1));
		Vector3d v = new Vector3d();
		v.scaleAdd(defaultValues.scale*i, a, v);
		v.scaleAdd(defaultValues.scale*j, b, v);
		v.scaleAdd(defaultValues.scale*k, c, v);
		
		BranchGroup atom = createAtom(v, defaultValues.dotSize3d);
		points[i+xMax][j+yMax][k+zMax] = new Point3d(v);
		atoms[i+xMax][j+yMax][k+zMax] = atom;
		isSelected[i+xMax][j+yMax][k+zMax] = false;
		intensity[i+xMax][j+yMax][k+zMax] = calcIntensity(i, j, k);
		dontdraw[i+xMax][j+yMax][k+zMax] = !visible;
		isProjected[i+xMax][j+yMax][k+zMax] = false;
		if (visible) netRoot.addChild(atom);
	}
	
	
	public void createLegend() {
		double h = defaultValues.scale*zMax*c.z+1;
		Transform3D t3l = new Transform3D();
		t3l.rotZ(Math.PI/2);
		TransformGroup tgl = new TransformGroup(t3l);
		tgl.addChild(Utils3d.createFixedLegend("Reciprocal lattice", new Point3d(0, 0, h+.2), .1f, Utils3d.createApp(blue), true));		
		tgl.addChild(Utils3d.createFixedLegend("points", new Point3d(0, 0, h), .1f, Utils3d.createApp(blue), true));		
		if (netLabel!=null) removeChild(netLabel);
		netLabel = new BranchGroup();
		netLabel.setCapability(BranchGroup.ALLOW_DETACH);
		netLabel.addChild(tgl);
		addChild(netLabel);
	}
	public void createRepere() {
		BranchGroup bb = new BranchGroup();
		bb.setCapability(BranchGroup.ALLOW_DETACH);
		bb.addChild(Utils3d.createRepere(cyan, blue, null, new String[] {"a*", "b*", "c*"}, .15f, .02f, defaultValues.dotSize, -defaultValues.dotSize, (Vector3d)Utils3d.mul(a, defaultValues.scale), (Vector3d)Utils3d.mul(b, defaultValues.scale), (Vector3d)Utils3d.mul(c, defaultValues.scale)));
		netRoot.addChild(bb);
		
		directRepere = new BranchGroup(); 
		directRepere.setCapability(BranchGroup.ALLOW_DETACH);
		Vector3d[] r = Lattice.reciprocal(a, b, c);
		r[0].normalize(); r[0].scaleBy(.3);
		r[1].normalize(); r[1].scaleBy(.3);
		r[2].normalize(); r[2].scaleBy(.3);
		directRepere.addChild(Utils3d.createRepere(red, red, null, new String[] {"a", "b", "c"}, .15f, .02f, defaultValues.dotSize, -defaultValues.dotSize, (Vector3d)Utils3d.mul(r[0], defaultValues.scale), (Vector3d)Utils3d.mul(r[1], defaultValues.scale), (Vector3d)Utils3d.mul(r[2], defaultValues.scale)));
		if (directShowed) {
			netRoot.addChild(directRepere);
		}
	}
	public void createTranspBox() {
		Appearance app = new Appearance();
		app.setMaterial(new Material(white, blue, black, blue, 120.0f));
		TransparencyAttributes transp = new TransparencyAttributes(TransparencyAttributes.NICEST, .85f);
		app.setTransparencyAttributes(transp);
		Box box = new Box(2, 2, 2, app);
		Transform3D t3d = new Transform3D();
		Matrix3d matrix = new Matrix3d();
		matrix.setColumn(0, (Vector3d)Utils3d.mul(a, x));
		matrix.setColumn(1, (Vector3d)Utils3d.mul(b, y));
		matrix.setColumn(2, (Vector3d)Utils3d.mul(c, z));
		t3d.set(matrix);
		TransformGroup tg = new TransformGroup(t3d);
		tg.addChild(box);
		netRoot.addChild(tg);
	}

	public class GonioHead extends BranchGroup {
		private OrientationObject orientationGonio;
		private Transform3D t3d;
		private TransformGroup tg;
		public GonioHead(OrientationClass orientationClass) {
			orientationGonio = orientationClass.new OrientationObject();
			t3d = new Transform3D();
			tg = new TransformGroup(t3d);
			tg.setCapabilityTo(TransformGroup.ALLOW_TRANSFORM_WRITE);
			tg.addChild(orientationGonio);
			tg.addChild(orientationGonio.tgOmegaOnly);
			addChild(tg);
			buildGadjet();
		}
		public void setY(double y) {
			t3d.set(new Vector3d(0, -y, 0));
			tg.setTransform(t3d);
		}
		private void buildGadjet() {
			Transform3D t3d1 = new Transform3D();
			t3d1.set(new Vector3d(0, 0, -.4));
			TransformGroup tg1 = new TransformGroup(t3d1);
			tg1.addChild(new Box(.2f, .2f, .1f, Utils3d.createApp(blue)));
			orientationGonio.tgOmegaOnly.addChild(new Torus(.05f, .6f, 10, 50, Utils3d.createApp(yellow)));
			orientationGonio.addChild(tg1);
			orientationGonio.addChild(Utils3d.createCylinder(new Point3d(), new Point3d(0, 0, -.4), .02, Utils3d.createApp(orange), 5));
		}
	}
	
	public synchronized void setLattice(Lattice l) {
		createNet(l.x, l.y, l.z, x, y, z);
	}
	public synchronized void setCrystalSize(int x, int y, int z) {
		createNet(a, b, c, x, y, z);
	}
	
	public synchronized Point3d getPoint(int h, int k, int l) {
		return points[h+xMax][k+yMax][l+zMax];
	}

	public synchronized void highlight(int h, int k, int l) {
		if (!isSelected[h+xMax][k+yMax][l+zMax]) {
			if (dontdraw[h+xMax][k+yMax][l+zMax]) netRoot.addChild(atoms[h+xMax][k+yMax][l+zMax]);
			changeAtomApp(atoms[h+xMax][k+yMax][l+zMax], redApp);
			isSelected[h+xMax][k+yMax][l+zMax] = true;
		}
	}
	public synchronized void unHighlight(int h, int k, int l) {
		if (isSelected[h+xMax][k+yMax][l+zMax]) {
			changeAtomApp(atoms[h+xMax][k+yMax][l+zMax], defaultApp);
			if (dontdraw[h+xMax][k+yMax][l+zMax]) netRoot.removeChild(atoms[h+xMax][k+yMax][l+zMax]);
			isSelected[h+xMax][k+yMax][l+zMax] = false;
		}
	}
	public synchronized void highlightGreen(int h, int k, int l) {
		changeAtomApp(atoms[h+xMax][k+yMax][l+zMax], greenApp);
	}
	public synchronized float intensity(int h, int k, int l) {
		return intensity[h+xMax][k+yMax][l+zMax];
	}
}


