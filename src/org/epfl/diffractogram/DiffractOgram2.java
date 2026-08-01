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
		super(true);
	}
	
	protected void setVersionValues() {
		setVersionValues2();
	}

	protected void setVersionValues2() { 
		title = "DiffractOgram2";
		DefaultValues.isUnitSphere = true;
		DefaultValues.directRays = true; //automatically true if isUnitSphere
		DefaultValues.developNet = true;
		DefaultValues.javaJmol = true;
		DefaultValues.finalizeDefaults();
	}
	
	public void showGoniometer(boolean show) {
		stop();
		if (show) {
			DiffractOgram.argJmol = true;
			super.setVersionValues();
		} else {
			setVersionValues2();
		}
		mainPane.resetLeftPanel();
		setFrameTitle();
	}
	
	public static void main(String[] args) {
		DiffractOgram mainApp = new DiffractOgram2();
		mainApp.init();
		mainApp.start();
	}


}
