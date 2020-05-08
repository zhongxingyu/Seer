 package gui;
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 /**
  * GUI for displaying a few buttons for adding
  * and creating buttons.
  * 
  * @author May Camp
  * @author Michelle Len
  * @author Jonathan Tan
  *
  */
 public class GUIAdd extends JPanel {
 
 	private static final long serialVersionUID = -5200304895970513817L;
 
 	private JPanel middlePanel;
 
 	private JButton createUnitButton = new JButton("Create Unit");
 	private JButton createVariableButton = new JButton("Create Variable");
 	private JButton createFormulaButton = new JButton("Create Formula");
 
 	public GUIAdd() {
 
 		setSize(720,480);
 		
		
		
 		middlePanel = new JPanel();
 
 		add(BorderLayout.CENTER, middlePanel);
 
 		JPanel actionPanel = new JPanel();
 		actionPanel.setOpaque(false);
 		
 		actionPanel.add(createUnitButton);
 		actionPanel.add(createVariableButton);
 		actionPanel.add(createFormulaButton);
 		
 		add(BorderLayout.NORTH,actionPanel);
 		
 		middlePanel.setOpaque(false);
 		
 		createUnitButton.addActionListener(new createUnitButtonListener());
 		createVariableButton.addActionListener(new createVariableButtonListener());
 		createFormulaButton.addActionListener(new createFormulaButtonListener());
 		
 	}
 	
 	// I'll just leave the action listeners like this for now, until
 	// GUI AddUnit, AddVariable, and AddFormula work.
 	class createUnitButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			remove(middlePanel);
 			GUIMain.updateUI();
 			middlePanel = new GUIAddUnit();
 			add(BorderLayout.CENTER,middlePanel);
 		}
 	}
 	
 	class createVariableButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			remove(middlePanel);
 			GUIMain.updateUI();
 			middlePanel = new GUIAddVariable();
 			add(BorderLayout.CENTER,middlePanel);
 		}
 	}
 	
 	class createFormulaButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			remove(middlePanel);
 			GUIMain.updateUI();
 			middlePanel = new GUIAddFormula();
 			add(BorderLayout.CENTER,middlePanel);
 		}
 	}
 	
 } // class GUIAdd
