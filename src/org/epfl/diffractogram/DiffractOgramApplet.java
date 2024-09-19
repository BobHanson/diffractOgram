package org.epfl.diffractogram;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.epfl.diffractogram.gui.MainPane;
import org.epfl.diffractogram.model3d.WorldRenderer;


@SuppressWarnings("serial")
public class DiffractOgramApplet extends JApplet implements Runnable {
	static final int width=1050, height=750;
	private static final String defCodeBase = "http://escher.epfl.ch/crystalOgraph/";
	
	protected String title;
	
	public static boolean isApplet = true;
	public JFrame frame;
	public MainPane mainPane;
	public boolean started;
	
	public DiffractOgramApplet() {
		setVersionValues();
		// not allowing indirect without unit sphere
		if (DefaultValues.isUnitSphere)
			DefaultValues.directRays = true;

		DefaultValues.useJmol = /** @j2sNative true || */DefaultValues.javaJmol;
		DefaultValues.showVersionDefaults();
	}
	
	public void init() {
		if (WorldRenderer.isJS) {
			run();
		} else {
			try {
				SwingUtilities.invokeAndWait(this);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}
	public void start() {
		started=true;
	}
	public void stop() {
		started=false;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainPane.stop();
			}
		});
	}
	
	public void destroy() {
		mainPane.destroy();
	}
	
	public static void main(String[] args) {
		isApplet = false;
		DiffractOgramApplet mainApp = new DiffractOgramApplet();
		mainApp.init();
		mainApp.start();
	}

	// initialisation in GUI thread
	public void run() {
		createMainFrame();
		if (isApplet) createWebPane();
		
		createMainPane();
		showMainPane();
		//new DropTarget(frame, new CifFileDropper(mainPane)); 
	}

	private void createMainFrame() {
	  frame = new JFrame(title + " is starting up. Please wait...");
	  frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isApplet && WorldRenderer.isJS) {
					stop();
					frame.setVisible(false);
				} else {
					System.exit(0);
				}
			}
	  });
	  //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(width, height);
	  frame.setVisible(true);
	}
	
	private void createWebPane() {
		if ("true".equals(getParameter("mini"))) {
			getContentPane().add(new AppletMiniPane());
		}
		else {
			getContentPane().add(new JLabel("Applet launched. Refresh page to load again...", JLabel.CENTER));
		}
	}
	
	private void createMainPane() {
		
		try {
			DefaultValues defaultValues = new DefaultValues(); 
			defaultValues.parseParameters(this);
			mainPane = new MainPane(defaultValues);
		} catch (Error e) {
			e.printStackTrace();
			showException(e);
			throw e;
		}
	}
	
	protected void setVersionValues() {
		title = "DiffractOgram";
		DefaultValues.isUnitSphere = false;
		DefaultValues.directRays = true;  //true if isUnitSphere
		DefaultValues.developNet = false;
		DefaultValues.javaJmol = false;
	}

	private void showMainPane() {
		frame.getContentPane().add(mainPane.toJPanel());
		frame.validate();
		frame.setTitle(title + (DefaultValues.useJmol ? "/Jmol" : ""));
	  frame.setVisible(true);
	  frame.toFront();
	}
	
	public URL getCodeBase() {
		//URL codeBase=null;
		try {
			return super.getCodeBase();
		} catch (Exception e) {
			try {
				return new URL(defCodeBase);
			} catch (MalformedURLException e1) {
				throw new RuntimeException(e1);
			}
		}
	}
	
	public void showUp() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
		  	if (!started) start();
				frame.setVisible(true);
				frame.toFront();
			}
		});
	}
		
	public void setDndDropListener(DropTargetListener listener) {
		new DropTarget(frame, listener);
	}
		
	public void showException(Throwable error) {
		ErrorPane errorPane = new ErrorPane();
		JFrame errorFrame = new JFrame("There was a problem");
		errorFrame.getContentPane().add(errorPane);
		errorFrame.setSize(500, 400);
		errorFrame.setVisible(true);
		if (error instanceof NoClassDefFoundError && error.getMessage().indexOf("javax/media/j3d")!=-1) {
			errorPane.out.println("Java3D is not installed on your computer.");
			errorPane.out.println("Please visit http://escher.epfl.ch/java3d to learn how to install it.");
			errorPane.out.println("");
		}
		error.printStackTrace(errorPane.out);
	}
	
	class ErrorPane extends JPanel {
		public PrintStream out;
		private JTextArea textArea;
		
		public ErrorPane() {
			textArea = new JTextArea();
			textArea.setEditable(false);
	    JScrollPane scrollPane = new JScrollPane(textArea);
			setLayout(new BorderLayout());
			add(scrollPane);
			
			out = new PrintStream(new OutputStream(){
				public void write(byte[] bb) throws IOException {
					write(bb, 0, bb.length);
				}
				public void write(byte[] bb, int off, int len) throws IOException {
					textArea.setText(textArea.getText()+new String(bb, off, len));
				}
				public void write(int b) throws IOException {
					textArea.setText(textArea.getText()+(char)b);
				}
			});
		}
	}

	class AppletMiniPane extends JPanel {
		public AppletMiniPane() {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
			  	if (!started) start();
			  	if (frame!=null) {
						frame.setVisible(true);
						frame.toFront();
			  	}
				}
			});
		}
		public void paint(Graphics g) {
			new ImageIcon(getClass().getResource("/applet-mini.png")).paintIcon(this, g, 0, 0);
		}
	}
}
