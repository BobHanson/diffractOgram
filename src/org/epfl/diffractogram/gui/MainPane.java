package org.epfl.diffractogram.gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.epfl.diffractogram.DefaultValues;
import org.epfl.diffractogram.model3d.Model3d;
import org.epfl.diffractogram.model3d.Model3d.Parameters;
import org.epfl.diffractogram.model3d.ProjScreen;

/* TestApplet - MainPane.java
 * 
 * Author   : Nicolas Schoeni
 * Creation : 23 nov. 06
 * 
 * nicolas.schoeni@epfl.ch
 */

public class MainPane extends HVPanel.VPanel {
	
	public interface DOG {
		void showGoniometer(boolean show);
	}
	
	public DOG dog2App;
	public Model3d model3d;
	private JSplitPane splitPane;
	private ProjScreen projected;
	private BottomPanel bottomPanel;
	@SuppressWarnings("unused")
	private DefaultValues defaultValues;
	
	public MainPane(DOG dogApp, DefaultValues defaultValues) {
		this.dog2App = dogApp;
		try {
			this.defaultValues = defaultValues;
			projected = new ProjScreen(this);
			projected.setMinimumSize(new Dimension(0, 0));

			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getLeftPanelAndModel(), projected);
			splitPane.setResizeWeight(0.6);
			splitPane.setMinimumSize(new Dimension(0, 0));
			splitPane.setContinuousLayout(true);
			// splitPane.setOneTouchExpandable(true);

			bottomPanel = new BottomPanel(defaultValues, model3d);
			expand(true);
			addComp(splitPane);
			expand(false);
			addSubPane(bottomPanel);
			model3d.complete();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public Model3d.Parameters getParameters() {
		return bottomPanel.getParameters();
	}

	public void setParameters(Parameters params) {
		model3d.targetN = params.targetN;
		bottomPanel.setParameters(params);
		model3d.doRays(false);
	}

	private Component getLeftPanelAndModel() {
		JPanel panel3d = new JPanel();
		panel3d.setMinimumSize(new Dimension(1, 1));
		panel3d.setLayout(new BorderLayout());
		model3d = new Model3d(this, panel3d, defaultValues, projected);
		return panel3d;
	}

	public void resetLeftPanel() {
		splitPane.setLeftComponent(getLeftPanelAndModel());
		model3d.complete();		
		bottomPanel.setModel(model3d);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("horizontal")) {
			changeSplitPane(JSplitPane.HORIZONTAL_SPLIT, 0.3);
		}
		if (e.getActionCommand().equals("vertical")) {
			changeSplitPane(JSplitPane.VERTICAL_SPLIT, 0.72);
		}
	}
	
	/**
	 * Added by Bob Hanson 2024.06.25 to return to the previous setting.
	 */
	private int[] splitPaneLoc = new int[] {-1, -1};
	
	private void changeSplitPane(int orientation, double weight) {
		splitPaneLoc[1 - orientation] = splitPane.getDividerLocation();
		splitPane.setOrientation(orientation);
		splitPane.setResizeWeight(weight);
		splitPane.resetToPreferredSizes();
		if (splitPaneLoc[orientation] > 0) {
			splitPane.setDividerLocation(splitPaneLoc[orientation] );
		}
		projected.setImageDefaultOrigin(orientation==JSplitPane.VERTICAL_SPLIT);
		projected.repaint();
	}
	
	public void stop() {
		if (bottomPanel.help!=null) bottomPanel.help.show(false);
		bottomPanel.animPane.animator.stopAnimation();
	}
	public void destroy() {
		model3d.destroy();
	}

	public void showGoniometer(boolean show) {
		if (dog2App != null)
			dog2App.showGoniometer(show);
	}

	public void echo(String s) {
		model3d.echo(s);
	}

}