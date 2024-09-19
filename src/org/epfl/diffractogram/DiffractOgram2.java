package org.epfl.diffractogram;

@SuppressWarnings("serial")
public class DiffractOgram2 extends DiffractOgram {

	public DiffractOgram2() {
		super();
	}
	
	protected void setVersionValues() {
		title = "DiffractOgram2";
		DefaultValues.isUnitSphere = true;
		DefaultValues.directRays = true; //automatically true if isUnitSphere
		DefaultValues.developNet = true;
		DefaultValues.javaJmol = true;
//		DefaultValues.useJmol = true;
	}

	public static void main(String[] args) {
		isApplet = false;
		DiffractOgram mainApp = new DiffractOgram2();
		mainApp.init();
		mainApp.start();
	}


}
