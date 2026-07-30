package org.epfl.diffractogram;

import swingjs.api.JSUtilI;

public class DiffractOgram2 extends DiffractOgram {

	public static boolean isJS = /** @j2sNative true || */
			false;

	public static JSUtilI jsutil;
	
	static {
		try {
			if (isJS) {
				jsutil = ((JSUtilI) Class.forName("swingjs.JSUtil").newInstance());
			}

		} catch (Exception e) {
			System.err.println("DiffractOgram2 could not create swinjs.JSUtil instance");
		}
	}

	
	public DiffractOgram2() {
		super();
	}
	
	protected void setVersionValues() {
		title = "DiffractOgram2";
		DefaultValues.isUnitSphere = true;
		DefaultValues.directRays = true; //automatically true if isUnitSphere
		DefaultValues.developNet = true;
		DefaultValues.javaJmol = true;
		DefaultValues.useJmol = true;
	}

	public static void main(String[] args) {
		isApplet = false;
		DiffractOgram mainApp = new DiffractOgram2();
		mainApp.init();
		mainApp.start();
	}


}
