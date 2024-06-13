/* DiffractOgram - Help.java
 * 
 * Author   : Nicolas Schoeni
 * Creation : 4 oct. 2005
 * 
 * nicolas.schoeni@epfl.ch
 */
package org.epfl.diffractogram.diffrac;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.epfl.diffractogram.bottomPanel.HVPanel;

public class Help {
	private JFrame frame;
	
	public Help() {
	  frame = new JFrame("DiffractOgram Help");
	  frame.setSize(800, 480);
	  frame.setVisible(false);
	  frame.setContentPane(new HelpPanel().toJPanel());
	  frame.validate();
	}
	
	public void show(boolean show) {
	  frame.setVisible(show);
	  if (show) frame.toFront();
	}
	private class HelpPanel extends HVPanel.v {
		JTextPane textPane;
		@SuppressWarnings("serial")
		public HelpPanel() {
			textPane = new JTextPane() {
				public synchronized void paint(Graphics g) {
					Graphics2D g2d = (Graphics2D)g;
					g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					super.paint(g);
				}
			};
			textPane.setEditable(false);
			addComp(new JScrollPane(textPane));
			try {
				URL helpURL = Help.class.getResource("/help.html");
				textPane.setPage(helpURL);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
