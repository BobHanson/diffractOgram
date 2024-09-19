package org.epfl.diffractogram.util;

import javax.media.j3d.Appearance;
import javax.vecmath.Color3f;

public class Colors {
	public static final Color3f black = new Color3f(0f, 0f, 0f);
	public static final Color3f gray = new Color3f(0.7f, 0.7f, 0.7f);
	public static final Color3f white = new Color3f(1f, 1f, 1f);
	public static final Color3f blue = new Color3f(0f, 0f, 1f);
	public static final Color3f red = new Color3f(1f, 0f, 0f);
	public static final Color3f yellow = new Color3f(1f, 1f, 0f);
	public static final Color3f green = new Color3f(0f, 1f, 0f);
	public static final Color3f cyan = new Color3f(0f, 1f, 1f);
	public static final Color3f magenta = new Color3f(1f, 0f, 1f);
	public static final Color3f orange = new Color3f(1f, .7f, 0f);
	
	public static final Appearance appBlack = Utils3d.createApp(Colors.black);
	public static final Appearance appBlue = Utils3d.createApp(Colors.blue);
	public static final Appearance appOrange = Utils3d.createApp(orange);
	public static final Appearance appRed = Utils3d.createApp(red);
	public static final Appearance appYellow = Utils3d.createApp(yellow);

}
