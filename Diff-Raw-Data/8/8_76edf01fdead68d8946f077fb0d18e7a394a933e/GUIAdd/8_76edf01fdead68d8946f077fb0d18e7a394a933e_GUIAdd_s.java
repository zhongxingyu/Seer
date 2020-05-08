 package gui;
 import java.awt.BorderLayout;
import java.awt.Panel;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;

 import javax.swing.JButton;
 import javax.swing.JPanel;
import javax.swing.SwingUtilities;
 
 /**
  * GUI for displaying a few buttons for adding
  * and creating buttons.
  * 
  * @author May Camp
  * @author Michelle Len
  *
  */
 public class GUIAdd extends JPanel {
 
 	private static final long serialVersionUID = -5200304895970513817L;
 
 	private NewtonsToolboxPanel middlePanel;
 
 	private JButton createUnitButton = new JButton("Create Unit");
 	private JButton createVariableButton = new JButton("Create Variable");
 	private JButton createFormulaButton = new JButton("Create Formula");
 
	private JPanel panel;

 	public GUIAdd() {
 
 		setSize(720,480);
 		
 		middlePanel = new NewtonsToolboxPanel();
 
 		add(BorderLayout.CENTER, middlePanel);
 
 		//need action listeners
 		middlePanel.add(createUnitButton);
 		middlePanel.add(createVariableButton);
 		middlePanel.add(createFormulaButton);
 		
 		createUnitButton.addActionListener(new createUnitButtonListener());
 		createVariableButton.addActionListener(new createVariableButtonListener());
 		createFormulaButton.addActionListener(new createFormulaButtonListener());
 		
 	}
 	
 	public class NewtonsToolboxPanel extends JPanel {
 		// Included to suppress Eclipse Warning
 		private static final long serialVersionUID = 5110086507916942106L;
 	}
 	
 	// I'll just leave the action listeners like this for now, until
 	// GUI AddUnit, AddVariable, and AddFormula work.
 	class createUnitButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			middlePanel.removeAll();
 			middlePanel.add(new GUIAddUnit());
 		}
 	}
 	
 	class createVariableButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			middlePanel.removeAll();
 			middlePanel.add(new GUIAddVariable());
 		}
 	}
 	
 	class createFormulaButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			middlePanel.removeAll();
 			middlePanel.add(new GUIAddFormula());
 		}
 	}
 	
 } // class GUIAdd
