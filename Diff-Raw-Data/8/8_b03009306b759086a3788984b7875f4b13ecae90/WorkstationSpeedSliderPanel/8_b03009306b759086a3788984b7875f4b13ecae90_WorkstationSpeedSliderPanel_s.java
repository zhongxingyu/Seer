 package gui.panels.subcontrolpanels;
 
 import gui.panels.ControlPanel;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.swing.BoxLayout;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 public class WorkstationSpeedSliderPanel extends JPanel {
 	
 	private static final long serialVersionUID = 7286657091005148296L;
 	
 	private ControlPanel controlPanel;
 	
 	private String[] workstations = {"Drill0", "Drill1", "Cross Seamer0", "Cross Seamer1", "Grinder0", "Grinder1"};
	private JComboBox<String> workstationComboBox;
 	
 	private JSlider speedSlider;
 	private String selected;
 	private Map<String,Integer> speeds;	
 	
 	public WorkstationSpeedSliderPanel(ControlPanel cp) {
 		super();
 		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
 		
 		controlPanel = cp;
 				
 		selected = "Drill0";
		workstationComboBox = new JComboBox<String>(workstations);
 		workstationComboBox.addActionListener(new ComboBoxListener());
 		
 		speeds = new HashMap<String,Integer>();
 		for (int i = 0; i < workstations.length; i++) {
 			speeds.put(workstations[i], 10);
 		}
 		
 		speedSlider = new JSlider(1, 50, 1);
 		speedSlider.setMajorTickSpacing(10);
 		speedSlider.setPaintLabels(true);
 		speedSlider.setToolTipText("" + speedSlider.getValue());
 		speedSlider.addChangeListener(new SliderListener());
 		
 		Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
 		table.put(1, new JLabel("Fastest"));
 		table.put(50, new JLabel("Slowest"));
 		speedSlider.setLabelTable(table);
 		
 		this.add(workstationComboBox);
 		this.add(speedSlider);
 		this.validate();
 	}
 	 
 	protected class SliderListener implements ChangeListener {
 
 		@Override
 		public void stateChanged(ChangeEvent evt) {
 			JSlider slider = (JSlider) evt.getSource();
 			int newSpeed = slider.getValue();
 			speedSlider.setToolTipText("" + newSpeed);
 			speeds.put(selected,newSpeed);
 			controlPanel.getGuiParent().getGUIOfflineWorkstations().get(selected).setSpeed(newSpeed);
 		}
 	}
 	
 	protected class ComboBoxListener implements ActionListener {
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public void actionPerformed(ActionEvent evt) {
			JComboBox<String> cb = (JComboBox<String>) evt.getSource();
 			selected = (String) cb.getSelectedItem();
 			speedSlider.setValue(speeds.get(selected));
 		}
 		
 	}
 
 }
