package org.epfl.diffractogram.bottomPanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.NumberFormatter;

public abstract class HVPanel implements ActionListener {
	public static boolean quiet = false;
	private ButtonGroup group;
	private ActionListener listener;
	private JPanel jPanel;
	
	public HVPanel() {
		jPanel = new JPanel();
		jPanel.setLayout(new GridBagLayout());
	}
	
	public JPanel toJPanel() {
		return jPanel;
	}
	
	public void setBorder(Border border) {
		jPanel.setBorder(border);
	}
	
	public static class HPanel extends HVPanel {
		private GridBagConstraints c;
		public HPanel() {
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.gridx=c.gridy=0; 
			c.weightx=c.weighty=0.01;
		}
		protected void increment() {
			c.gridx++;
			c.gridy=0; 
		}
		protected void subIncrement() {
			c.gridy++;
		}
		public GridBagConstraints c() {
			return c;
		}
		public void left() {
			c.anchor = GridBagConstraints.WEST;
			c.weightx = 0;
		}
		public void right() {
			c.anchor = GridBagConstraints.EAST;
			c.weightx = 0;
		}
		public void putExtraSpace() {
			putExtraSpace(10);
		}
		public void putExtraSpace(int pixels) {
			if (c.fill==GridBagConstraints.BOTH || c.fill==GridBagConstraints.HORIZONTAL) {
				double wx = c.weightx;
				c.weightx = 1;
				addComp(new JPanel());
				c.weightx = wx;
			}
			else {
				int l = c.insets.left;
				c.insets.left = pixels;
				addComp(new JPanel());
				c.insets.left = l;
			}
		}
		public void expand(boolean b) {
			if (c.fill==GridBagConstraints.BOTH || c.fill==GridBagConstraints.VERTICAL) {
				c.fill = b?GridBagConstraints.BOTH:GridBagConstraints.VERTICAL;
			} else {
				c.fill = b?GridBagConstraints.HORIZONTAL:GridBagConstraints.NONE;
			}
			c.weightx = b?1:0;
		}
	}	
		
	public static class VPanel extends HVPanel {
		private GridBagConstraints c;
		public VPanel() {
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(0,2,0,2);  
			c.gridx=c.gridy=0; 
			c.weightx=c.weighty=0.01;
		}
		protected void increment() {
			c.gridy++;
			c.gridx=0; 
		}
		protected void subIncrement() {
			c.gridx++;
		}
		public GridBagConstraints c() {
			return c;
		}
		public void top() {
			c.anchor = GridBagConstraints.NORTH;
			c.weighty = 0;
		}
		public void bottom() {
			c.anchor = GridBagConstraints.SOUTH;
			c.weighty = 0;
		}
		public void putExtraSpace() {
			putExtraSpace(10);
		}
		public void putExtraSpace(int pixels) {
			if (c.fill==GridBagConstraints.BOTH || c.fill==GridBagConstraints.VERTICAL) {
				double wy = c.weighty;
				c.weighty = 1;
				addComp(new JPanel());
				c.weighty = wy;
			}
			else {
				int t = c.insets.top;
				c.insets.top = pixels;
				addComp(new JPanel());
				c.insets.top = t;
			}
		}
		public void expand(boolean b) {
			if (c.fill==GridBagConstraints.BOTH || c.fill==GridBagConstraints.HORIZONTAL) {
				c.fill = b?GridBagConstraints.BOTH:GridBagConstraints.HORIZONTAL;
			} else {
				c.fill = b?GridBagConstraints.VERTICAL:GridBagConstraints.NONE;
			}
			c.weighty = b?1:0;
		}
	}	

	protected abstract void increment();
	protected abstract void subIncrement();
	public abstract GridBagConstraints c();

	
	
	public void addComp(Component comp) {
		jPanel.add(comp, c());
		increment();
	}
	
	public void addSubPane(HVPanel panel) {
		addComp(panel.jPanel);
		panel.addActionListener(this);
	}

	public void addButton(AbstractButton button) {
		addComp(button);
		button.addActionListener(this);
	}
	public void addButtonGroupped(AbstractButton button) {
		if (group==null) {
			group = new ButtonGroup();
			button.setSelected(true);
		}
		group.add(button);
		addButton(button);
	}
	

	public EditField addIntFieldSpinner(String name, String unit, int nbcol, int defValue) {
		return new EditField(name, unit, nbcol, defValue, this, true);
	}

	public EditField addIntField(String name, String unit, int nbcol, int defValue) {
		return new EditField(name, unit, nbcol, defValue, this, false);
	}

	public EditField addStringField(String name, String unit, int nbcol, String defValue) {
		return new EditField(name, unit, nbcol, defValue, this, false);
	}

	public EditField addSpecialFormatField(String name, String unit, int nbcol, String defValue, AbstractFormatter formatter) {
		return new EditField(name, unit, nbcol, defValue, formatter, this, false);
	}
	public EditField addSpecialFormatField(String name, String unit, int nbcol, Object defValue, AbstractFormatter formatter) {
		return new EditField(name, unit, nbcol, defValue, formatter, this, false);
	}

	public EditField addFloatField(String name, String unit, int nbcol, float defValue, String format) {
		return new EditField(name, unit, nbcol, defValue, format, this, false);
	}

	public SliderAndValue addSliderAndValueH(String name, String unit, double min, double max, double def, int nbDecimals, int size) {
		return new SliderAndValueH(name, unit, min, max, def, nbDecimals, size, this);
	}
	public SliderAndValue addSliderAndValueV(String name, String unit, double min, double max, double def, int nbDecimals, int size) {
		return new SliderAndValueV(name, unit, min, max, def, nbDecimals, size, this);
	}
	
	public void addActionListener(ActionListener listener) {
		this.listener = listener;
	}

	public void actionPerformed(ActionEvent e) {
		if (listener!=null) {
			if (e.getSource() instanceof SliderAndValue)
				listener.actionPerformed(e);
			else if (e.getSource() instanceof HVPanel)
				listener.actionPerformed(e);
			else if (e.getSource() instanceof AbstractButton)
				listener.actionPerformed(new ActionEvent(e.getSource(), ((AbstractButton)e.getSource()).isSelected()?1:0, ((AbstractButton)e.getSource()).getText()));
			else if (e.getSource() instanceof EditField)
				listener.actionPerformed(e);
		}
	}
	
	public static void main(String[] args) {
	}

	public class EditField implements PropertyChangeListener, ActionListener {
		private ActionListener listener;
		private JSpinner jspinner;
		public JFormattedTextField edit;
		public JLabel name, unit;
		
		public EditField(String name, String unit, int nbcol, String defValue, AbstractFormatter f, ActionListener listener, boolean spinner) {
			createField(name, unit, nbcol, defValue, f, listener, spinner);
		}
		public EditField(String name, String unit, int nbcol, Object defValue, AbstractFormatter f, ActionListener listener, boolean spinner) {
			createField(name, unit, nbcol, defValue, f, listener, spinner);
		}
		
		public EditField(String name, String unit, int nbcol, String defValue, ActionListener listener, boolean spinner) {
			DefaultFormatter f = new DefaultFormatter();
			f.setOverwriteMode(false);
			createField(name, unit, nbcol, defValue, f, listener, spinner);
		}
		
		public EditField(String name, String unit, int nbcol, int defValue, ActionListener listener, boolean spinner) {
			NumberFormatter f = new NumberFormatter(java.text.NumberFormat.getIntegerInstance());
			createField(name, unit, nbcol, new Integer(defValue), f, listener, spinner);
		}
		
		public EditField(String name, String unit, int nbcol, float defValue, String format, ActionListener listener, boolean spinner) {
			DecimalFormat df = new DecimalFormat(format);
			DecimalFormatSymbols s = df.getDecimalFormatSymbols();
			s.setDecimalSeparator('.');
			df.setDecimalFormatSymbols(s);
			NumberFormatter f = new NumberFormatter(df);
			createField(name, unit, nbcol, new Float(defValue), f, listener, spinner);
		}

		private void createField(String name, String unit, int nbcol, Object defValue, AbstractFormatter format, ActionListener listener, boolean spinner) {
			this.listener = listener;
			if (spinner) {
				jspinner = new JSpinner();
				edit = ((DefaultEditor)jspinner.getEditor()).getTextField();
			}
			else {
				edit = new JFormattedTextField(format);
			}
			edit.setValue(defValue);
			edit.setColumns(nbcol);
			edit.addActionListener(this);
			edit.addPropertyChangeListener(this);
			edit.setMinimumSize(new Dimension(30, 20));
			this.name = new JLabel(name);
			jPanel.add(this.name, c());
			c().gridx++;
			jPanel.add(spinner?(Component)jspinner:edit, c());
			c().gridx++;
			if (unit!=null) {
				this.unit = new JLabel(unit);
				jPanel.add(this.unit, c());
				c().gridx++;
			}
			increment();
		}

		public void actionPerformed(ActionEvent e) { 
			//System.out.println(e);
			//listener.actionPerformed(new ActionEvent(this, 0, name.getText()));
		}
		
		public void propertyChange(PropertyChangeEvent evt) {
			if (!quiet && evt.getPropertyName().equals("value") && !edit.getText().equals(evt.getNewValue())) {
				listener.actionPerformed(new ActionEvent(this, 0, name.getText()));
			}
		}
		
		public float getFloatValue() {
			return Float.parseFloat(""+edit.getValue());
		}
		
		public Object getValue() {
			return edit.getValue();
		}
		public void setValue(Object v) {
			edit.setValue(v);
		}
		
		public void setEnable(boolean b) {
			if (jspinner!=null) jspinner.setEnabled(b);
			edit.setEditable(b);
			edit.setEnabled(b);
			if (name!=null) name.setEnabled(b);
			if (unit!=null) unit.setEnabled(b);
		}
	}	

	
	public class SliderAndValueH extends SliderAndValue {
		public SliderAndValueH(String name, String unit, double min, double max, double def, int nbDecimals, int size, ActionListener listener) {
			super(name, unit, min, max, def, nbDecimals, true, size, listener);	
		}
		protected void increment(GridBagConstraints c) {
			c.gridy++;
			c.gridx=0; 
		}
		protected void subIncrement(GridBagConstraints c) {
			c.gridx++;
		}
	}
	public class SliderAndValueV extends SliderAndValue {
		public SliderAndValueV(String name, String unit, double min, double max, double def, int nbDecimals, int size, ActionListener listener) {
			super(name, unit, min, max, def, nbDecimals, false, size, listener);	
		}
		protected void increment(GridBagConstraints c) {
			c.gridx++;
			c.gridy=0; 
		}
		protected void subIncrement(GridBagConstraints c) {
			c.gridy++;
		}
	}
	
	public abstract class SliderAndValue implements ChangeListener, MouseWheelListener, ActionListener, PropertyChangeListener {
		private JSlider slider;
		private JFormattedTextField edit;
		private JLabel nameLabel, unitLabel;
		private ActionListener listener;
		private double mult;
		
		public SliderAndValue(String name, String unit, double min, double max, double def, int nbDecimals, boolean orientation, int size, ActionListener listener) {
			this.listener=listener;
			mult = Math.pow(10, nbDecimals);
			slider = new JSlider(orientation?JSlider.HORIZONTAL:JSlider.VERTICAL, (int)Math.round(min*mult), (int)Math.round(max*mult), (int)Math.round(def*mult));
			slider.setMinimumSize(new Dimension(orientation?size:0, orientation?0:size));
						
			if (name!=null && name.length()!=0) {
				jPanel.add(nameLabel = new JLabel(name), c());
				nameLabel.addMouseWheelListener(this);
				subIncrement(c());
			}
			
			jPanel.add(slider, c());
			slider.addChangeListener(this);
			slider.addMouseWheelListener(this);
			subIncrement(c());

			if (unit!=null) {
				StringBuffer sf = new StringBuffer("0");
				if (nbDecimals>0) sf.append('.');
				for(int i=0; i<nbDecimals; i++) sf.append('0');
				DecimalFormat df = new DecimalFormat(sf.toString());
				DecimalFormatSymbols s = df.getDecimalFormatSymbols();
				s.setDecimalSeparator('.');
				df.setDecimalFormatSymbols(s);
				NumberFormatter formatter = new NumberFormatter(df);
				formatter.setMinimum(new Double(min));
				formatter.setMaximum(new Double(max));
				
				edit = new JFormattedTextField(formatter);
				edit.setValue(new Double(def));
				edit.setColumns(3);
				edit.setMinimumSize(new Dimension(40, 0));
				jPanel.add(edit, c());
				edit.addPropertyChangeListener(this);
				edit.addActionListener(this);
				edit.addMouseWheelListener(this);
				subIncrement(c());
				if (unit.length()!=0) {
					jPanel.add(unitLabel = new JLabel(unit), c());
					unitLabel.addMouseWheelListener(this);
				}
			}
			increment(c());
		}

		protected abstract void subIncrement(GridBagConstraints c);
		protected abstract void increment(GridBagConstraints c);
		
		public void setValue(double v) {
			slider.setValue((int)Math.round(v*mult));
		}
		public double getValue() {
			return slider.getValue()/mult;
		}
		public double getMin() {
			return ((Double)((NumberFormatter)edit.getFormatter()).getMinimum()).doubleValue();
		}
		public double getMax() {
			return ((Double)((NumberFormatter)edit.getFormatter()).getMaximum()).doubleValue();
		}
		
		public void stateChanged(ChangeEvent e) { 
	  	double v = getValue();
			if (edit!=null)  {
				edit.setValue(new Double(v));
				try {
					edit.commitEdit();
				} catch (Exception ex) {}
			}
			if (!quiet && listener!=null) listener.actionPerformed(new ActionEvent(this, 0, nameLabel==null?null:(nameLabel.getText())));
		}

		public void mouseWheelMoved(MouseWheelEvent e) {
			setValue(getValue()+e.getWheelRotation()/mult);
		}

		public void actionPerformed(ActionEvent e) { 
			setValue(((Double)edit.getValue()).doubleValue());
		}
		public void propertyChange(PropertyChangeEvent evt) {
			if (!quiet && evt.getPropertyName().equals("value") && !edit.getText().equals(evt.getNewValue())) {
				setValue(((Double)edit.getValue()).doubleValue());
			}
		}
		public void setEnabled(boolean b) {
			if (edit!=null) edit.setEnabled(b);
			if (slider!=null) slider.setEnabled(b);
			if (nameLabel!=null) nameLabel.setEnabled(b);
		}
	}
}