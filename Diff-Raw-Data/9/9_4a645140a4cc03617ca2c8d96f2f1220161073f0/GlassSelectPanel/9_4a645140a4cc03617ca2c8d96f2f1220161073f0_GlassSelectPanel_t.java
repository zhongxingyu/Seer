 package gui.panels.subcontrolpanels;
 
 import engine.agent.BinRobotAgent;
 import gui.panels.ControlPanel;
 
import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import shared.Glass;
 import shared.enums.MachineType;
 
 /**
  * The GlassSelectPanel class contains buttons allowing the user to select what type of glass to produce.
  */
 @SuppressWarnings("serial")
 public class GlassSelectPanel extends JPanel implements ActionListener {
 	/** The ControlPanel this is linked to */
 	private ControlPanel parent;
 
 	private JScrollPane scrollPane;
 	private JPanel mainPanel;
 	private ArrayList<JCheckBox> checkBoxes;
 	private JButton goButton = new JButton("GO!");
 	private BinRobotAgent binRobot;
 	
 	/**
 	 * Creates a new GlassSelect and links it to the control panel
 	 * 
 	 * @param cp the ControlPanel linked to it
 	 */
 	public GlassSelectPanel(ControlPanel cp) {
 		parent = cp;
		
 		checkBoxes = new ArrayList<JCheckBox>();
 		checkBoxes.add(new JCheckBox(MachineType.CUTTER.toString()));
 		checkBoxes.add(new JCheckBox(MachineType.BREAKOUT.toString()));
 		checkBoxes.add(new JCheckBox(MachineType.MANUAL_BREAKOUT.toString()));
 		checkBoxes.add(new JCheckBox(MachineType.DRILL.toString()));
 		checkBoxes.add(new JCheckBox(MachineType.CROSS_SEAMER.toString()));
 		checkBoxes.add(new JCheckBox(MachineType.GRINDER.toString()));
 		checkBoxes.add(new JCheckBox(MachineType.WASHER.toString()));
 		checkBoxes.add(new JCheckBox(MachineType.PAINT.toString()));
 		checkBoxes.add(new JCheckBox(MachineType.UV_LAMP.toString()));
 		checkBoxes.add(new JCheckBox(MachineType.OVEN.toString()));
 		
 		mainPanel = new JPanel();
 		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
 		
		mainPanel.setBackground(Color.gray);
		
 		for (int i=0; i<checkBoxes.size(); i++) {
 			//checkBoxes.get(i).setAlignmentX(CENTER_ALIGNMENT);
 			mainPanel.add(checkBoxes.get(i));
 		}
 		//goButton.setAlignmentX(CENTER_ALIGNMENT);
 		mainPanel.add(goButton);
 		
 		mainPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);		
 		scrollPane = new JScrollPane(mainPanel);
 		this.add(scrollPane);
 		
 		mainPanel.validate();
 		scrollPane.validate();
 		
 		// Add action listeners
 		goButton.addActionListener(this);		
 	}
 
 	public void actionPerformed(ActionEvent ae) {
 		// Tell the bin robot to queue another piece of glass
 		if (ae.getSource() == goButton) {
 			binRobot.msgHereIsGlass(getGlassFromMenu());
 		}
 	}
 
 	private Glass getGlassFromMenu() {
 		ArrayList<MachineType> mTypes = new ArrayList<MachineType>();
 		
 		for (JCheckBox c : checkBoxes) {
 			if (c.isSelected()) {
 				mTypes.add(MachineType.getTypeFromString(c.getText()));
 //				c.setSelected(false); // optionally reset
 			}
 		}
 		return new Glass(mTypes);
 	}
 
 	/**
 	 * Returns the parent panel
 	 * 
 	 * @return the parent panel
 	 */
 	public ControlPanel getGuiParent() {
 		return parent;
 	}
 
 	/**
 	 * Sets bin robot of glass select panel, which needs it for creating new glasses from the gui
 	 */
 	public void setBinRobot(BinRobotAgent b) {
 		binRobot = b;
 	}	
 }
