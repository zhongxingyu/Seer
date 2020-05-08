 package myComponent;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.ButtonGroup;
 import javax.swing.JRadioButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import transmissions.ScalingOptions;
 
 /**
  * Puts together all a curve panel and the fields to interact with it.
  * 
  * @author MichaelZ
  *
  */
 @SuppressWarnings("serial")
 public class CurvePanelEnvironment extends JPanel {
 
 	
 	private CurvePanel _curvePanel;
 	private float _customExp = 0.5f;
 
 	String _prefix;
 	
 	public CurvePanelEnvironment(String prefix) {
 		_prefix = prefix;
 		addContent();
 		this.setBorder(BorderFactory.createLineBorder(Color.gray, 2));
 	}
 	
 
 
 	public ScalingOptions getScalingOptions() {
 		return _curvePanel.getScalingOptions();
 	}
 	
 	
 
 	private void addContent() {
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 
 		c.gridx = 0;
 		c.gridy = 0;
 		JPanel drawPanel = new JPanel(new GridBagLayout());
 		this.add(drawPanel);
 		
 			fillDrawPanel(drawPanel);
 		
 		c.gridx = 1;
 		c.gridy = 0;
 		JPanel optionPanel = new JPanel();
 		optionPanel.setLayout(new GridBagLayout());
 		this.add(optionPanel, c);
 
 //OPTION//PANEL//		
 	//STANDARD
 			GridBagConstraints c3 = new GridBagConstraints();
 			
 			c3.gridx = 0;
 			c3.anchor = GridBagConstraints.LINE_START;
 			c3.gridwidth = GridBagConstraints.REMAINDER;
 			
 			//Label
 			c3.gridy = 0;
 			JLabel labelStandard = new JLabel("standard");
 			labelStandard.setFont(new Font("Arial", Font.PLAIN, 16));
 			optionPanel.add(labelStandard, c3);
 		
 			//option1 standard exp
 			c3.gridy = 1;
 			JRadioButton radioStandardExp = new JRadioButton("^ (x^0.333)");
 			radioStandardExp.setSelected(true);
 			radioStandardExp.addChangeListener(new ChangeListener() {
 				@Override
 				public void stateChanged(ChangeEvent e) {
 					if (((JRadioButton)e.getSource()).isSelected()) {
						_curvePanel.setExponent(0.333f);
 					}
 				}			
 			});
 			optionPanel.add(radioStandardExp, c3);
 			
 			//option2 standard linear
 			c3.gridy = 2;
 			JRadioButton radioStandardLinear = new JRadioButton("linear (cut > 0.8)");
 			radioStandardLinear.setSelected(false);
 			radioStandardLinear.addChangeListener(new ChangeListener() {
 				@Override
 				public void stateChanged(ChangeEvent e) {
 					if (((JRadioButton)e.getSource()).isSelected()) {
 						_curvePanel.setControlPoints(new float[][] {{0.8f, 1}, {1,1}, {1,1}, {1,1}});
 					}
 				}			
 			});
 			optionPanel.add(radioStandardLinear, c3);
 			
 
 
 			c3.gridy = 3;
 			optionPanel.add(Box.createRigidArea(new Dimension(0,10)), c3);		
 			
 			
 	//EXP CUSTOM
 			c3.gridy = 4;
 			JLabel labelExp = new JLabel("exp");
 			labelExp.setFont(new Font("Arial", Font.PLAIN, 16));
 			optionPanel.add(labelExp, c3);	
 			
 			//select custom exp
 			c3.gridwidth = 1;	
 			c3.gridy = 5;
 			c3.gridx = 0;
 			final JTextField textCustomExp = new JTextField(String.valueOf(_customExp),5);
 			final JRadioButton radioCustomExp = new JRadioButton("x^");
 			
 			radioCustomExp.addChangeListener(new ChangeListener() {
 				@Override
 				public void stateChanged(ChangeEvent e) {
 					if (radioCustomExp.isSelected()) {
 						_curvePanel.setExponent(_customExp);
 					}
 				}
 			});
 			optionPanel.add(radioCustomExp, c3);
 			
 			//setup entry field for custom exp
 			c3.gridx = 1;
 			textCustomExp.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					testCustomExpField(textCustomExp, radioCustomExp);
 				}
 			});			
 			textCustomExp.addFocusListener(new FocusListener() {
 				@Override
 				public void focusGained(FocusEvent arg0) {
 				}
 				@Override
 				public void focusLost(FocusEvent arg0) {
 					testCustomExpField(textCustomExp, radioCustomExp);
 				}
 			});
 			optionPanel.add(textCustomExp, c3);
 
 			c3.gridx = 0;
 			c3.gridwidth = GridBagConstraints.REMAINDER;			
 			c3.gridy = 6;
 			optionPanel.add(Box.createRigidArea(new Dimension(0,10)), c3);
 			
 	//LINEAR CUSTOM
 			final ControlPointComponent controlPointComp = new ControlPointComponent();
 			_curvePanel.registerClickListener(controlPointComp);	
 				
 			c3.gridy = 7;
 			JLabel labelLinear = new JLabel("linear");
 			labelLinear.setFont(new Font("Arial", Font.PLAIN, 16));
 			optionPanel.add(labelLinear, c3);
 			
 			//select custom linear
 			c3.gridy = 8;
 			final JRadioButton radioCustomLinear = new JRadioButton("use points");
 			radioCustomLinear.addChangeListener(new ChangeListener() {
 				@Override
 				public void stateChanged(ChangeEvent e) {
 					if (radioCustomLinear.isSelected()) {
 						if (controlPointComp.isConsistent()) {
 							_curvePanel.setControlPoints(controlPointComp.getControlPoints());
 						}
 					}
 				}
 			});
 			optionPanel.add(radioCustomLinear, c3);
 			
 			
 			c3.gridx = 1;
 			c3.gridy = 9;
 			optionPanel.add(controlPointComp, c3);
 			controlPointComp.registerChangeListener(new ControlPointsChangeListener() {
 				@Override
 				public void pointsChanged(boolean isConsistent) {
 					if (isConsistent) {
 						radioCustomLinear.setSelected(true);
 						_curvePanel.setControlPoints(controlPointComp.getControlPoints());
 					}
 				}
 			});
 		
 			
 			ButtonGroup bg = new ButtonGroup();
 			bg.add(radioStandardLinear);
 			bg.add(radioStandardExp);
 			bg.add(radioCustomExp);
 			bg.add(radioCustomLinear);
 	}
 
 	
 	/**
 	 * checks if the value entered into the text field is > 0 and float parsable.
 	 * If this is the case sets the radioButton if not changes the field to ""
 	 * 
 	 * @param textCustomExp
 	 * @param radioCustomExp
 	 */
 	private void testCustomExpField(JTextField textCustomExp, JRadioButton radioCustomExp) {
 		float exponent = -1.0f;				
 		try {
 			exponent = Float.parseFloat(textCustomExp.getText());
 		} catch (Exception exception) {}
 		
 		if (exponent > 0) {
 			_customExp = (float)exponent;
 			radioCustomExp.setSelected(true);
 			_curvePanel.setExponent(_customExp);
 		} else {
 			textCustomExp.setText("");
 		}
 	}
 	
 	/**
 	 * @param drawPanel is filled with the {@link CurvePanel} and additional annotations
 	 */
 	private void fillDrawPanel(JPanel drawPanel) {
 		GridBagConstraints c2 = new GridBagConstraints();
 
 		
 		c2.gridx = 0;
 		c2.gridy = 1;
 		JLabel scaled = new JLabel("<html>s<br>c<br>a<br> l<br>e<br>d<br> <br>v<br>a<br> l<br>u<br>e<br>s</html>");
 		scaled.setFont(new Font("dialog.bold", Font.BOLD, 11));
 		drawPanel.add(scaled, c2);	
 
 		c2.gridx = 0;
 		c2.gridy = 2;
 		JLabel nullnull = new JLabel("(0,0)");
 		drawPanel.add(nullnull, c2);	
 
 		c2.gridx = 1;
 		c2.gridy = 1;
 		_curvePanel = new CurvePanel(_prefix);
 		drawPanel.add(_curvePanel, c2);
 
 		c2.gridx = 1;
 		c2.gridy = 2;
 		JLabel normed = new JLabel("normalized values");
 		drawPanel.add(normed, c2);	
 			
 		c2.gridx = 2;
 		c2.gridy = 0;
 		JLabel einseins = new JLabel("(1,1)");
 		drawPanel.add(einseins, c2);
 	}
 
 
 	
 	
 }
