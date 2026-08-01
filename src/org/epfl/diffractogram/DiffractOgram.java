package org.epfl.diffractogram;

import java.awt.BorderLayout;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.epfl.diffractogram.gui.MainPane;
import org.epfl.diffractogram.gui.MainPane.DOG;
import org.epfl.diffractogram.model3d.WorldRenderer;

@SuppressWarnings("serial")
public class DiffractOgram implements Runnable, DOG {
	static final int width = 1050, height = 750;
//	private static final String defCodeBase = "http://escher.epfl.ch/crystalOgraph/";

	protected String title;

	protected static boolean argJmol;

	public JFrame frame;
	public MainPane mainPane;
	public boolean started;

	public DiffractOgram() {
		this(false);
	}

	public DiffractOgram(boolean isDOG2) {
		DefaultValues.isDOG2 = isDOG2;
		setVersionValues();
	}

	/**
	 * Overridden in DiffractOgram2
	 * 
	 */
	protected void setVersionValues() {
		title = "DiffractOgram";
		DefaultValues.isUnitSphere = false;
		DefaultValues.directRays = true;
		DefaultValues.developNet = false;
		DefaultValues.javaJmol = DiffractOgram.argJmol;
		DefaultValues.finalizeDefaults();
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
		started = true;
	}

	public void stop() {
		started = false;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainPane.stop();
			}
		});
	}

	public void destroy() {
		frame.remove(mainPane.getJPanel());
		mainPane.destroy();
	}

	// initialisation in GUI thread
	public void run() {
		createAndShowFrame();
	}

	private void createAndShowFrame() {
		frame = new JFrame(title + " is starting up. Please wait...");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(width, height);
		frame.setVisible(true);

		try {
			DefaultValues defaultValues = new DefaultValues();
			mainPane = new MainPane(DefaultValues.isDOG2 ? this : null, defaultValues);
			frame.getContentPane().add(mainPane.getJPanel());
			frame.validate();
			setFrameTitle();
			frame.setVisible(true);
			frame.toFront();
			// new DropTarget(frame, new CifFileDropper(mainPane));
		} catch (Error e) {
			e.printStackTrace();
			showException(e);
			throw e;
		}
	}

	protected void setFrameTitle() {
		frame.setTitle(title + (DefaultValues.useJmol ? "/Jmol" : ""));
	}

	public void setDndDropListener(DropTargetListener listener) {
		new DropTarget(frame, listener);
	}

	private void showException(Throwable error) {
		ErrorPane errorPane = new ErrorPane();
		JFrame errorFrame = new JFrame("There was a problem");
		errorFrame.getContentPane().add(errorPane);
		errorFrame.setSize(500, 400);
		errorFrame.setVisible(true);
		if (error instanceof NoClassDefFoundError && error.getMessage().indexOf("javax/media/j3d") != -1) {
			errorPane.out.println("Java3D is not installed on your computer.");
			errorPane.out.println("Please visit http://escher.epfl.ch/java3d to learn how to install it.");
			errorPane.out.println("");
		}
		error.printStackTrace(errorPane.out);
	}

	private class ErrorPane extends JPanel {
		public PrintStream out;
		private JTextArea textArea;

		public ErrorPane() {
			textArea = new JTextArea();
			textArea.setEditable(false);
			JScrollPane scrollPane = new JScrollPane(textArea);
			setLayout(new BorderLayout());
			add(scrollPane);

			out = new PrintStream(new OutputStream() {
				public void write(byte[] bb) throws IOException {
					write(bb, 0, bb.length);
				}

				public void write(byte[] bb, int off, int len) throws IOException {
					textArea.setText(textArea.getText() + new String(bb, off, len));
				}

				public void write(int b) throws IOException {
					textArea.setText(textArea.getText() + (char) b);
				}
			});
		}
	}

	public static void main(String[] args) {
		argJmol = (args.length > 0 && "jmol".equalsIgnoreCase("" + args[0]));
		DiffractOgram mainApp = new DiffractOgram();
		mainApp.init();
		mainApp.start();
	}

	@Override
	public void showGoniometer(boolean show) {
		// DiffractoGram2 only
	}

}
