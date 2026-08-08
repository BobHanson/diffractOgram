package org.epfl.diffractogram.model3d;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;

import org.epfl.diffractogram.gui.MainPane;
import org.epfl.diffractogram.model3d.Model3d.Parameters;

/**
 * This is the screen on the right, with the x/y axes
 *
 */
@SuppressWarnings("serial")
public class ProjScreen extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener {
	private int paintW, paintH, mouseX, mouseY, mouseX0, mouseY0, mouseX0S, mouseY0S;
	private double width, height;
	int iw, ih;
	public Image image;
	private boolean firstPaint;
	private boolean fixedWidth;

	private List<Dot> dots;

	private MainPane main;

	private static class Dot {
		public static Comparator<Dot> sorter = new Comparator<Dot>() {

			@Override
			public int compare(Dot d2, Dot d1) {
				return (  d1.i > d2.i ? 1 : d1.i < d2.i ? -1 
						: d1.j > d2.j ? 1 : d1.j < d2.j ? -1 
						: d1.k > d2.k ? 1 : d1.k < d2.k ? -1 : 0);
			}
			
		};
		Double point;
		float pointSize;
		int i, j, k, n;
		private String coord;
		private Parameters params;

		public Dot(Double p, float s, int i, int j, int k, int n, Parameters params) {
			this.point = p;
			this.pointSize = s;
			this.i = i;
			this.j = j;
			this.k = k;
			this.n = n;
			this.params = params;
			params.targetN = n;
		}

		public boolean is(int i, int j, int k) {
			return (this.i == i && this.j == j && this.k == k);
		}
		
		public String getCoord() {
			return (coord == null ? (coord = "(" + i + " " + j + " " + k + ")") : coord);
		}
		
		public String toString() {
			return getCoord() + " " + n  + " " + params+ " " + point;
		}
	}

	public ProjScreen(MainPane main) {
		this.main = main;
		firstPaint = true;
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addComponentListener(sizeListener);
	}

	public synchronized void setImageSize(double w, double h, boolean fixedWidth) {

		dots = new ArrayList<Dot>();
		this.width = w;
		this.height = h;
		this.fixedWidth = fixedWidth;
		iw = roundToPower2(w * 64);
		ih = roundToPower2(h * 64);
		setImageDefaultOrigin(fixedWidth);
		repaint();

	}

	public synchronized void setImageDefaultOrigin(boolean fixedWidth) {
		Dimension screen = getSize();
		if (!fixedWidth && height / (screen.height) > width / screen.width) {
			paintH = screen.height;
			paintW = (int) Math.round(paintH * width / height);
			mouseY = 0;
			mouseX = (screen.width - paintW) / 2;
		} else {
			paintW = screen.width;
			paintH = (int) Math.round(paintW * height / width);
			mouseX = 0;
			mouseY = (screen.height - paintH) / 2;
		}
	}

	public synchronized void clearImage() {
		dots.clear();
		repaint();
	}

	private String index = "";

	private void showIndex(int x, int y) {
		List<Dot> indexVect = getIndexes(x, y);
		if (indexVect == null) {
			main.echo("");
			return;	
		}
		String s = "";
		for (int i = 0; i < indexVect.size(); i++)
			s += indexVect.get(i).getCoord() + " ";
		s = s.trim();
		if (!s.equals(index)) {
			setToolTipText(s);
			main.echo(s);
			index = s;
		}
	}

	private void showRay(int x, int y) {
		List<Dot> indexVect = getIndexes(x, y);
		if (indexVect == null)
			return;
		Dot dot = indexVect.get(0);
		main.setParameters(dot.params);
		main.echo(dot.getCoord());
		
	}

	private List<Dot> getIndexes(int x, int y) {
		String s = "";
		double xx = ((double) x - mouseX - paintW / 2) / paintW;
		double yy = ((double) y - mouseY - paintH / 2) / paintH;
		double e = .01;
		Dot d = findClosestDot(xx, yy, e);
		if (d == null)
			return null;
		List<Dot> indexVect = new ArrayList<Dot>();
		xx = d.point.x;
		yy = d.point.y;
		for (int i = 0; i < dots.size(); i++) {
			Dot dot = dots.get(i);
			Double pt = dot.point;
			if (near(pt, xx, yy, e)) {
				String sindex = dot.getCoord() + " ";
				if (s.contains(sindex)) {
					// BH 2024.06.27 duplicates here during precession
				} else {
					s += sindex;
					indexVect.add(dot);
				}
			}
		}
		if (indexVect.size() > 1)
			indexVect.sort(Dot.sorter);
		return indexVect;
	}

	private Dot findClosestDot(double xx, double yy, double e) {
		double min = 1e10;
		Dot minDot = null;
		for (int i = 0; i < dots.size(); i++) {
			Dot dot = dots.get(i);
			Double pt = dot.point;
			if (near(pt, xx, yy, e)) {
				double d = (pt.x - xx)* (pt.x - xx) + (pt.y - yy) * (pt.y - yy);
				if (d < min) {
					min = d;
					minDot = dot;
				}					
			}
		}
		return minDot;
	}

	private boolean near(Double pt, double x, double y, double e) {
		return Math.abs(pt.x - x) < e && Math.abs(pt.y - y) < e;
	}

	public synchronized void paint(Graphics g) {
		// does not fire while animating
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (firstPaint) {
			setImageDefaultOrigin(fixedWidth);
			firstPaint = false;
		}
		// BH: for some reason, the background was black
		Dimension size = getSize();
		g.setColor(getBackground());
		g.fillRect(0, 0, size.width, size.height);
		g.setColor(new Color(255, 255, 255));
		g.fillRect(mouseX, mouseY, paintW, paintH);

		// BH in JavaScript we must dispose after clipping
		Graphics g1 = g.create();
		g1.setClip(mouseX, mouseY, paintW, paintH);
		for (int i = 0; i < dots.size(); i++) {
			Dot dot = dots.get(i);
			Double p = dot.point;
			paintPoint(g1, p.x, p.y, mouseX, mouseY, paintW, paintH, dot.pointSize, false, i);
		}
		g1.dispose();
		g.setColor(Color.black);
		g.drawLine(mouseX + paintW / 2, mouseY, mouseX + paintW / 2, min(mouseY + paintH, getHeight()));
		g.drawLine(mouseX, mouseY + paintH / 2, mouseX + paintW, mouseY + paintH / 2);
	}

	private int min(int a, int b) {
		return a < b ? a : b;
	}

	private synchronized void paintPoint(Graphics g, double x, double y, int px, int py, int pw, int ph, float s,
			boolean selected, int index) {
		if (index >= 0) {
			for (int i = 0; i < dots.size(); i++) {
				if (i == index)
				    continue;
				Dot dot = dots.get(i);
				if (dot.pointSize > s) {
					if (near(dot.point, x, y, 0.001)) {
						return;
					}
				}
			}
		}

		int w = Math.round(s * 6) - 1;
		int x1 = (int) Math.round(x * pw + pw / 2.0 + px - w / 2);
		int y1 = (int) Math.round(y * ph + ph / 2.0 + py - w / 2);
		// if (x1+w<(px<0?0:px) || y1+w<(py<0?0:py) || x1>(px<0?pw+px:pw*2) ||
		// y1>(py<0?ph+py:ph*2)) return;
		if (x1 + w < mouseX || y1 + w < mouseY || x1 > mouseX + paintW || y1 > mouseY + paintH)
			return;

		if (selected) {
			g.setColor(Color.red);
		} else {
			int c = Math.round(255 - s * 255);
			if (c < 0)
				c = 0;
			g.setColor(new Color(c, c, c));
		}

		// System.out.println(x+" "+y);
		// g.setColor(Color.red);
		// BH necessary to enclose setClip with create/dispose in JavaScript
		// BH but I don't see why this clip is necessary, actually
		// BH moved this clip to the calling method.
		// g1.setClip(px < 0 ? 0 : px, py < 0 ? 0 : py, px < 0 ? pw + px : pw, py < 0 ?
		// ph + py : ph);
		g.fillOval(x1, y1, w, w);
		// if (ig!=null)
		// ig.fillOval((int)Math.round(x*pw+pw/2.0+px-w/2),
		// (int)Math.round(y*ph+ph/2.0+py-w/2), w, w);
	}

	public synchronized void drawPoint(Graphics mg, Double p, float s, int i, int j, int k, int n) {
		if (mg == null || near(p, 0, 0, 0.01))
			return;
		double x = p.x;
		double y = p.y;
		for (int ii = 0; ii < dots.size(); ii++) {
			Dot dot = dots.get(ii);
			if (dot.is(ii,  j,  k)) {
				Double pi = dot.point;
				if (near(pi, x, y, 0.001)) {
					return;
				}
				// points may be reflected in x or y axis
			}
		}
		// BH more efficient
		dots.add(new Dot(p, s, i, j, k, n, main.getParameters()));
		paintPoint(mg, x, y, mouseX, mouseY, paintW, paintH, s, false, -1);
	}

	public synchronized void mouseDragged(MouseEvent e) {
		if (e.isControlDown() || e.isAltDown()) {
			int a, b, d;
			a = e.getX() - mouseX0S;
			b = e.getY() - mouseY0S;
			if (Math.abs(a) > Math.abs(b))
				d = a;
			else
				d = b;
			if (d > 0 || (paintW >= 20 && paintH >= 20)) {
				mouseX -= paintW / 10 * d / 2;
				mouseY -= paintH / 10 * d / 2;
				paintW += paintW / 10 * d;
				paintH += paintH / 10 * d;
			}
			repaint();
		} else {
			mouseX += (e.getX() - mouseX0);
			mouseX0 = e.getX();
			mouseY += (e.getY() - mouseY0);
			mouseY0 = e.getY();
			repaint();
		}
		mouseX0S = e.getX();
		mouseY0S = e.getY();
	}

	public synchronized void mouseMoved(MouseEvent e) {
		mouseX0 = e.getX();
		mouseY0 = e.getY();
		mouseX0S = e.getX();
		mouseY0S = e.getY();
		// hmm not during a paint?
		showIndex(mouseX0, mouseY0);
	}

	public synchronized void mouseClick(MouseEvent e) {
		mouseX0 = e.getX();
		mouseY0 = e.getY();
		mouseX0S = e.getX();
		mouseY0S = e.getY();
		// hmm not during a paint?
		showIndex(mouseX0, mouseY0);
	}

	public synchronized void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() < 0 && (paintW <= 20 || paintH <= 20))
			return;
		mouseX -= paintW / 10 * e.getWheelRotation() / 2;
		mouseY -= paintH / 10 * e.getWheelRotation() / 2;
		paintW += paintW / 10 * e.getWheelRotation();
		paintH += paintH / 10 * e.getWheelRotation();
		repaint();
	}

	public static int roundToPower2(double d) {
		return (int) Math.pow(2, (int) Math.round(Math.log(d) / Math.log(2.0)));
	}

	ComponentListener sizeListener = new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
			setImageDefaultOrigin(fixedWidth);
			// BH added to fix clicking [ ] (maximize) button on frame in Java and general
			// resize update in JavaScript
			// BH as well as issues with moving the splitpane bar in Java and JavaScript
			Dimension size = ProjScreen.this.getSize();
			if (size.width > 0)
				ProjScreen.this.paintImmediately(0, 0, size.width, size.height);

		}
	};

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseX0 = e.getX();
		mouseY0 = e.getY();
		mouseX0S = e.getX();
		mouseY0S = e.getY();
		// hmm not during a paint?
		showRay(mouseX0, mouseY0);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
