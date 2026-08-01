package org.epfl.diffractogram;

@SuppressWarnings("serial")
public class DiffractOgram2Applet extends DiffractOgramApplet {

	public DiffractOgram2Applet() {
		super();
	}
	
	protected void setVersionValues() {
		title = "DiffractOgram2" + (isApplet ? "/applet" : "");
		DefaultValues.isUnitSphere = true;
		DefaultValues.directRays = true; //automatically true if isUnitSphere
		DefaultValues.developNet = true;
		DefaultValues.javaJmol = true;
		DefaultValues.useJmol = true;
		DefaultValues.finalizeDefaults();
	}

	public static void main(String[] args) {
		isApplet = false;
		DiffractOgramApplet mainApp = new DiffractOgram2Applet();
		mainApp.init();
		mainApp.start();
	}


}
