package org.epfl.diffractogram.projScreen;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Vector;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ProjScreen extends JPanel implements MouseMotionListener, MouseWheelListener {
	private int paintW, paintH, paintX, paintY, paintX0, paintY0, paintX0S, paintY0S;
	private double width, height;
	int iw, ih;
	public Image image;
	private Vector<Serializable> points, index_ijk;
	private Vector<Float> pointSizes;
	private boolean firstPaint;
	//private Raster blank;
	private Graphics mg;//, ig;
	private boolean fixedWidth;
	
	public ProjScreen() {
		firstPaint = true;
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addComponentListener(sizeListener);
	}
	
	public synchronized void setImageSize(double w, double h, boolean fixedWidth) {

		this.width = w;
		this.height = h;
		this.fixedWidth = fixedWidth;
		iw = roundToPower2(w*64);
		ih = roundToPower2(h*64);
		image = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.setColor(new Color(100, 100, 100, 100));
		g.fillRect(0, 0, iw, ih);
		
		g.setColor(Color.black);
		//blank = ((BufferedImage)image).getData();

		points = new Vector<Serializable>(10, 100);
		pointSizes = new Vector<Float>(10, 100);
		index_ijk = new Vector<Serializable>(10, 100);
		setImageDefaultOrigin(fixedWidth);
		repaint();

	}
		
	public synchronized void setImageDefaultOrigin(boolean fixedWidth) {
		Dimension screen = getSize();
		if (!fixedWidth && height/(screen.height)>width/screen.width) {
			paintH = screen.height;
			paintW = (int)Math.round(paintH*width/height);
			paintY = 0;
			paintX = (screen.width-paintW)/2;
		} 
		else {
			paintW = screen.width;
			paintH = (int)Math.round(paintW*height/width);
			paintX = 0;
			paintY = (screen.height-paintH)/2;
		}
		mg = getGraphics();
/*
		if (screen.width>0 && screen.height>0) {
			image = new BufferedImage(screen.width, screen.height, BufferedImage.TYPE_INT_ARGB);
			ig = image.getGraphics();
			ig.setColor(new Color(100, 100, 100, 100));
			ig.fillRect(0, 0, screen.width, screen.height);
		}
*/		
	}
	
	public synchronized void clearImage() {
		//((BufferedImage)image).setData(blank);
		points.clear();
		pointSizes.clear();
		index_ijk.clear();
		repaint();
	}
		
	private String index = "";
	private Vector<Integer> indexVect;
	private void showIndex(int x, int y) {
		double xx = ((double)x-paintX-paintW/2)/paintW;
		double yy = ((double)y-paintY-paintH/2)/paintH;
		double e = .015;
		String s = "";
		indexVect = new Vector<Integer>(10, 10);
		for (int i=0; i<points.size(); i++) {
			if (Math.abs(((Point.Double)points.get(i)).x-xx)<e && Math.abs(((Point.Double)points.get(i)).y-yy)<e) {
				indexVect.add(new Integer(i));
				int index = ((Integer)index_ijk.get(i)).intValue();
				s+="("+(((index>>16)&0xff)-128)+" "+(((index>>8)&0xff)-128)+" "+((index&0xff)-128)+") ";
			}
		}
		if (!s.equals(index)) {
			//paint(mg);
			
			mg.setClip(0, getSize().height-20, getSize().width, 20);
			mg.clearRect(0, getSize().height-20, getSize().width, 20);
			mg.setColor(Color.black);
			mg.drawString(s, 5, getSize().height-5);
			index = s;
		}
	}
	
	public synchronized void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (firstPaint) {
			setImageDefaultOrigin(fixedWidth);
			firstPaint = false;
		}
		
		//g.drawImage(image, paintX, paintY, paintW, paintH, null);
		
		g.setColor(new Color(255, 255, 255, 200));
		g.fillRect(paintX, paintY, paintW, paintH);
		
		Point.Double p;
		for (int i=0; i<points.size(); i++) {
			//boolean selected = (indexVect!=null && indexVect.contains(new Integer(i)));
			p = (Point.Double)points.get(i);
			paintPoint(g, p.x, p.y, paintX, paintY, paintW, paintH, ((Float)pointSizes.get(i)).floatValue(), false, i);
		}

		
		g.setColor(Color.black);
		g.drawLine(paintX+paintW/2, paintY, paintX+paintW/2, min(paintY+paintH, getHeight()));
		g.drawLine(paintX, paintY+paintH/2, paintX+paintW, paintY+paintH/2);
	}
	
	private int min(int a, int b) {
		return a<b?a:b;
	}
	
	private synchronized void paintPoint(Graphics g, double x, double y, int px, int py, int pw, int ph, float s, boolean selected, int index) {
		if (index>=0) {
			for (int i=0; i<pointSizes.size(); i++) {
				if (i!=index && ((Float)(pointSizes.get(i))).floatValue()>s) {
					Point.Double p = (Point.Double)points.get(i);
					if (Math.abs(p.x-x)<0.001 && Math.abs(p.y-y)<0.001) {
						return;
					}
				}
			}
		}

		int w=Math.round(s*6)-1;
		int x1 = (int)Math.round(x*pw+pw/2.0+px-w/2);
		int y1 = (int)Math.round(y*ph+ph/2.0+py-w/2);
		//if (x1+w<(px<0?0:px) || y1+w<(py<0?0:py) || x1>(px<0?pw+px:pw*2) || y1>(py<0?ph+py:ph*2)) return;
		if (x1+w<paintX || y1+w<paintY || x1>paintX+paintW || y1>paintY+paintH) return;

		if (selected) {
			g.setColor(Color.red);
		}
		else {
			int c=Math.round(255-s*255);
			if (c<0) c = 0;
			g.setColor(new Color(c, c, c));
		}
		
		//System.out.println(x+" "+y);
		//g.setColor(Color.red);
		
		g.setClip(px<0?0:px, py<0?0:py, px<0?pw+px:pw, py<0?ph+py:ph);
		g.fillOval(x1, y1, w, w);

		//if (ig!=null)
			//ig.fillOval((int)Math.round(x*pw+pw/2.0+px-w/2), (int)Math.round(y*ph+ph/2.0+py-w/2), w, w);
	}
	
	public synchronized void drawPoint(Point.Double p, float s, byte i, byte j, byte k) {
		if (mg==null) return;
		
		if (Math.abs(p.x)<.01 && Math.abs(p.y)<.01) return;
		int index = (((int)(i+128))<<16)+(((int)(j+128))<<8)+((int)(k+128));
		for (int ii=0; ii<index_ijk.size(); ii++) 
			if (((Integer)index_ijk.get(ii)).intValue()==index) {
				Point.Double pi = (Point.Double)points.get(ii);
				if (Math.abs(p.x-pi.x)<0.001 && Math.abs(p.y-pi.y)<0.001) {
					return;
				}
			}
		
		paintPoint(mg, p.x, p.y, paintX, paintY, paintW, paintH, s, false, -1);
		//paintPoint(image.getGraphics(), p.x, p.y, 0, 0, iw, ih, s);
		points.add(p);
		pointSizes.add(new Float(s));
		index_ijk.add(new Integer(index));
	}
	
	
	public synchronized void mouseDragged(MouseEvent e) {
		if (e.isControlDown() || e.isAltDown()) {
			int a, b, d;
			a = e.getX()-paintX0S;
			b = e.getY()-paintY0S;
			if (Math.abs(a)>Math.abs(b)) d=a;
			else d=b;
			if (d>0 || (paintW>=20 && paintH>=20)) {
				paintX-=paintW/10*d/2;
				paintY-=paintH/10*d/2;
				paintW+=paintW/10*d;
				paintH+=paintH/10*d;
			}
			repaint();
		}
		else {
			paintX+=(e.getX()-paintX0);
			paintX0=e.getX();
			paintY+=(e.getY()-paintY0);
			paintY0=e.getY();
			repaint();
		}
		paintX0S = e.getX();
		paintY0S = e.getY();
	}
	public synchronized void mouseMoved(MouseEvent e) {
		paintX0 = e.getX();
		paintY0 = e.getY();
		paintX0S = e.getX();
		paintY0S = e.getY();
		
		showIndex(paintX0, paintY0);
	}
	
	public synchronized void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation()<0 && (paintW<=20 || paintH<=20)) return;
		paintX-=paintW/10*e.getWheelRotation()/2;
		paintY-=paintH/10*e.getWheelRotation()/2;
		paintW+=paintW/10*e.getWheelRotation();
		paintH+=paintH/10*e.getWheelRotation();
		repaint();
	}
	
	public static int roundToPower2(double d) {
		return (int)Math.pow(2, (int)Math.round(Math.log(d)/Math.log(2.0)));		
	}
	
	ComponentListener sizeListener = new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
			setImageDefaultOrigin(fixedWidth);
		}
	};
}


