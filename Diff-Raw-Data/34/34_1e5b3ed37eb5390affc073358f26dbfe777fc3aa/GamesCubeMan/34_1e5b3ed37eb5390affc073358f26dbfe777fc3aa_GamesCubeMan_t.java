 package edu.berkeley.gcweb.gui.gamescubeman.XYZCube;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
import netscape.javascript.JSObject;

 import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.Canvas3D;
 import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.RotationMatrix;
 import edu.berkeley.gcweb.gui.gamescubeman.XYZCube.XYZCube.CubeVariation;
 
 @SuppressWarnings("serial")
 public class GamesCubeMan extends JApplet implements ChangeListener, ActionListener, CubeStateChangeListener {
 	private XYZCube cube;
 	private CubeCanvas cubeCanvas;
 	private Canvas3D canvas;
 	private JSlider scale, distance, gap, turningRate;
 	private JSpinner[] dimensions;
 	private JCheckBox antialiasing, colorChooserCheckBox;
 	private JButton resetView, scramble, resetCube;
 	private JTextField stateField;
 	private JRadioButton[] variationButtons;
 	
	private JSObject jso;
 	public void init() {
 		try {
 			SwingUtilities.invokeAndWait(new Runnable() {
 				public void run() {
 					cube = new XYZCube(3,3,3);
 					cube.addStateChangeListener(GamesCubeMan.this);
 					cubeCanvas = new CubeCanvas(cube);
 					canvas = cubeCanvas.getCanvas();
 					resetRotation();
 					
 					JPanel buttons = new JPanel();
 					buttons.setLayout(new BoxLayout(buttons, BoxLayout.PAGE_AXIS));
 					
 					resetView = new JButton("Reset View");
 					resetView.setMnemonic(KeyEvent.VK_R);
 					resetView.setFocusable(false);
 					resetView.addActionListener(GamesCubeMan.this);
 					buttons.add(resetView);
 					
 					resetCube = new JButton("Reset Cube");
 					resetCube.setFocusable(false);
 					resetCube.addActionListener(GamesCubeMan.this);
 					buttons.add(resetCube);
 					
 					scramble = new JButton("Scramble");
 					scramble.setMnemonic(KeyEvent.VK_S);
 					scramble.setFocusable(false);
 					scramble.addActionListener(GamesCubeMan.this);
 					buttons.add(scramble);
 					
 					antialiasing = new JCheckBox("Antialiasing", canvas.isAntialiasing());
 					antialiasing.setMnemonic(KeyEvent.VK_A);
 					antialiasing.setFocusable(false);
 					antialiasing.addActionListener(GamesCubeMan.this);
 					buttons.add(antialiasing);
 					
 					colorChooserCheckBox = new JCheckBox("Choose colors", cubeCanvas.isColorEditing());
 					colorChooserCheckBox.setMnemonic(KeyEvent.VK_C);
 					colorChooserCheckBox.setFocusable(false);
 					colorChooserCheckBox.addActionListener(GamesCubeMan.this);
 					buttons.add(colorChooserCheckBox);
 					
 					JPanel sliders = new JPanel();
 					sliders.setLayout(new BoxLayout(sliders, BoxLayout.PAGE_AXIS));
 					
 					distance = new JSlider(4, 200, (int) (cube.getCenter()[2]));
 					distance.setFocusable(false);
 					distance.addChangeListener(GamesCubeMan.this);
 					sliders.add(sideBySide(new JLabel("Distance"), distance));
 					
 					scale = new JSlider(0, 10000, canvas.getScale());
 					scale.setFocusable(false);
 					scale.addChangeListener(GamesCubeMan.this);
 					sliders.add(sideBySide(new JLabel("Scale"), scale));
 					
 					int[] dim = cube.getDimensions();
 					dimensions = new JSpinner[dim.length];
 					JPanel dims = new JPanel();
 					for(int ch = 0; ch < dimensions.length; ch++) {
 						dimensions[ch] = new JSpinner(new SpinnerNumberModel(dim[ch], 1, null, 1));
 						((JSpinner.NumberEditor)dimensions[ch].getEditor()).getTextField().setFocusable(false);
 						dimensions[ch].getEditor().setFocusable(false);
 						dimensions[ch].addChangeListener(GamesCubeMan.this);
 						if(ch != 0)
 							dims.add(new JLabel("x"));
 						dims.add(dimensions[ch]);
 					}
 					sliders.add(dims);
 					
 					gap = new JSlider(0, 50, (int)(100*cube.getStickerGap()));
 					gap.setFocusable(false);
 					gap.addChangeListener(GamesCubeMan.this);
 					sliders.add(sideBySide(new JLabel("Gap"), gap));
 					
 					turningRate = new JSlider(1, cube.getMaxTurningRate(), cube.getTurningRate());
 					turningRate.setMajorTickSpacing(1);
 					turningRate.setMinorTickSpacing(1);
 					turningRate.setPaintTicks(true);
 					turningRate.setFocusable(false);
 					turningRate.addChangeListener(GamesCubeMan.this);
 					sliders.add(sideBySide(new JLabel("Speed"), turningRate));
 					
 					ButtonGroup g = new ButtonGroup();
 					CubeVariation[] vars = CubeVariation.values();
 					variationButtons = new JRadioButton[vars.length];
 					for(int i = 0; i < variationButtons.length; i++) {
 						variationButtons[i] = new JRadioButton(vars[i].toString(), vars[i] == cube.getCubeVariation());
 						variationButtons[i].setActionCommand(""+i);
 						variationButtons[i].setFocusable(false);
 						variationButtons[i].addActionListener(GamesCubeMan.this);
 						g.add(variationButtons[i]);
 					}
 					sliders.add(sideBySide(variationButtons));
 					
 					JPanel topHalf = new JPanel(new BorderLayout());
 					topHalf.add(sideBySide(buttons, sliders), BorderLayout.PAGE_START);
 					
 					stateField = new JTextField(cube.getState());
 					topHalf.add(stateField, BorderLayout.CENTER);
 
 					JPanel pane = new JPanel(new BorderLayout());
 					setContentPane(pane);
 					
 					pane.add(topHalf, BorderLayout.PAGE_START);
 					pane.add(cubeCanvas, BorderLayout.CENTER);
 					canvas.requestFocusInWindow();
 					
 					setBG(pane, Color.GRAY);
 				}
 			});
			jso = JSObject.getWindow(this);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void setBG(JComponent comp, Color c) {
 		if(comp instanceof JButton)
 			return;
 		comp.setBackground(c);
 		for(Component child : comp.getComponents()) {
 			if(child instanceof JComponent)
 				setBG((JComponent)child, c);
 			else if(!(child instanceof JButton))
 				child.setBackground(c);
 		}
 	}
 	
 	private JPanel sideBySide(JComponent... cs) {
 		JPanel p = new JPanel();
 		p.setBackground(Color.WHITE);
 		for(JComponent c : cs)
 			p.add(c);
 		return p;
 	}
 	
 	private boolean green = false;
 	public void touch() {
		if(jso != null)
			jso.eval("alert('Cube Reset!');");
 //			jso.eval("update('hooow');"); //this and the next line are equivalent
 //			jso.call("update", new Object[] {"hiya"});
 		green = !green;
 //		cube.setBackground(green ? Color.GREEN : Color.WHITE);
 //		cube.repaint();
 	}
 
 	public void stateChanged(ChangeEvent e) {
 		Object src = e.getSource();
 		if(src == gap) {
 			cube.setStickerGap(gap.getValue() / 100.);
 		} else if(src == distance) {
 			double[] center = cube.getCenter();
 			center[2] = distance.getValue();
 			cube.setCenter(center[0], center[1], center[2]);
 		} else if(src == scale) {
 			canvas.setScale(scale.getValue());
 		} else if(e.getSource() == turningRate) {
 			cube.setTurningRate(turningRate.getValue());
 		} else if(src instanceof JSpinner) {
 			cube.setDimensions((Integer) dimensions[0].getValue(), (Integer) dimensions[1].getValue(), (Integer) dimensions[2].getValue());
 			cube.setCubeVariation(getSelectedVariation());
 		} 
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == resetView)
 			resetRotation();
 		else if(e.getSource() == scramble)
 			cube.scramble();
		else if(e.getSource() == resetCube) {
			touch();
 			cube.resetCube();
		}
 		else if(e.getSource() == antialiasing)
 			canvas.setAntialiasing(antialiasing.isSelected());
 		else if(e.getSource() == colorChooserCheckBox)
 			cubeCanvas.setColorEditing(colorChooserCheckBox.isSelected());
 		else if(e.getSource() instanceof JRadioButton)
 			cube.setCubeVariation(getSelectedVariation());
 	}
 	private CubeVariation getSelectedVariation() {
 		for(int i = 0; i < variationButtons.length; i++)
 			if(variationButtons[i].isSelected())
 				return CubeVariation.values()[i];
 		return null;
 	}
 
 	private void resetRotation() {
 		cube.setRotation(new RotationMatrix(0, -45));
 		canvas.mousePressed(null);
 	}
 
 	public void stateChanged(XYZCube src) {
 		stateField.setText(src.getState());
 	}
 	
 	public static void main(String[] args) {
 		final GamesCubeMan a = new GamesCubeMan();
 		a.init();
 		a.setPreferredSize(new Dimension(400, 500));
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				JFrame f = new JFrame();
 				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 				JPanel pane = new JPanel();
 				f.setContentPane(pane);
 				f.add(a);
 				f.pack();
 				f.setVisible(true);
 				a.canvas.requestFocusInWindow();
 			}
 		});
 	}
 }
