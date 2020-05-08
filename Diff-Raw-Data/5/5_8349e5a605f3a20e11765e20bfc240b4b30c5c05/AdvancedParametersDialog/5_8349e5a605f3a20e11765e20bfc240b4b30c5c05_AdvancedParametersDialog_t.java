 package View;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import net.miginfocom.swing.MigLayout;
 
 import Model.Airport;
 import Model.AirportObserver;
 import Model.Obstacle;
 import Model.PhysicalRunway;
 
 @SuppressWarnings("serial")
 public class AdvancedParametersDialog extends JDialog {
 
 	@SuppressWarnings("unused")
 	private Obstacle obstacle, obstacle_backup;
 	private JTextField tfRESA;
 	private JTextField tfStopway;
 	private JTextField tfBlastAllowance;
 	private JTextField tfAngleOfSlope;
 	private JTextField tfRunwayStripWidth;
 	private JTextField tfClearAndGradedWidth;
 	private JTextField tfObstacleWidth;
 	private JTextField tfObstacleLength;
 	
 	private Airport airportCopy;
 	private PhysicalRunway physicalRunway;
 	private List<AirportObserver> airportObservers;
 	
 	public AdvancedParametersDialog(Airport airport, List<AirportObserver> airportObservers) {
 		setResizable(false);
 		setTitle("Advanced Parameters");
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 310, 370);
 		
 		this.airportCopy = airport;
 		physicalRunway = airport.getCurrentPhysicalRunway();
 		this.airportObservers = airportObservers;
 		
 		JPanel contentPane = new JPanel();
 		setContentPane(contentPane);
 		contentPane.setLayout(new MigLayout("", "[100px:n,grow]", "[15px,grow][]"));
 		
 		JPanel fieldsPanel = new JPanel();
 		contentPane.add(fieldsPanel, "cell 0 0,grow");
		fieldsPanel.setLayout(new MigLayout("", "[170px][90px][12px]", "[19px][19px][19px][19px][19px][19px][0.45in,grow][19px][19px]"));
 		
 		
 		JLabel lblRESA = new JLabel("RESA");
 		fieldsPanel.add(lblRESA, "cell 0 0,alignx right,aligny center");
 		
 		tfRESA = new JTextField();
 		fieldsPanel.add(tfRESA, "cell 1 0,growx,aligny top");
 
 		
 		JLabel labelm0 = new JLabel("m");
 		fieldsPanel.add(labelm0, "cell 2 0,alignx left,aligny center");
 		
 		
 		JLabel lblStopway = new JLabel("Stopway");
 		fieldsPanel.add(lblStopway, "cell 0 1,alignx right,aligny center");
 		
 		tfStopway = new JTextField();
 		fieldsPanel.add(tfStopway, "cell 1 1,growx,aligny top");
 
 		
 		JLabel labelm1 = new JLabel("m");
 		fieldsPanel.add(labelm1, "cell 2 1,alignx left,aligny center");
 		
 		
 		JLabel lblBlastAllowance = new JLabel("Blast allowance");
 		fieldsPanel.add(lblBlastAllowance, "cell 0 2,alignx right,aligny center");
 		
 		tfBlastAllowance = new JTextField();
 		fieldsPanel.add(tfBlastAllowance, "cell 1 2,grow");
 
 		
 		JLabel labelm2 = new JLabel("m");
 		fieldsPanel.add(labelm2, "cell 2 2,alignx left,aligny center");
 		
 		
 		JLabel lblAngleOfSlope = new JLabel("Angle of slope");
 		fieldsPanel.add(lblAngleOfSlope, "cell 0 3,alignx right,aligny center");
 		
 		tfAngleOfSlope = new JTextField();
 		fieldsPanel.add(tfAngleOfSlope, "cell 1 3,growx,aligny top");
 
 		
 		JLabel labelm3 = new JLabel("m");
 		fieldsPanel.add(labelm3, "cell 2 3,alignx left,aligny center");
 		
 		
 		JLabel labelRunwayStripWidth = new JLabel("Runway Strip Width");
 		fieldsPanel.add(labelRunwayStripWidth, "cell 0 4,alignx right,aligny center");
 		
 		tfRunwayStripWidth = new JTextField();
 		fieldsPanel.add(tfRunwayStripWidth, "cell 1 4,growx,aligny top");
 	
 		
 		JLabel labelm4 = new JLabel("m");
 		fieldsPanel.add(labelm4, "cell 2 4,alignx left,aligny center");
 		
 		
 		JLabel lblClearAndGradedWidth = new JLabel("Clear and Graded Width");
		fieldsPanel.add(lblClearAndGradedWidth, "cell 0 5,alignx right,aligny center");
 		
 		tfClearAndGradedWidth = new JTextField();
 		fieldsPanel.add(tfClearAndGradedWidth, "cell 1 5,growx,aligny top");
 
 		
 		JLabel labelm5 = new JLabel("m");
 		fieldsPanel.add(labelm5, "cell 2 5,alignx left,aligny center");
 		
 		
 		JLabel lblNewLabel = new JLabel("Obstacle Width");
 		fieldsPanel.add(lblNewLabel, "cell 0 7,alignx right,aligny center");
 		
 		tfObstacleWidth = new JTextField();
 		fieldsPanel.add(tfObstacleWidth, "cell 1 7,growx,aligny top");
 		
 
 		JLabel labelm6 = new JLabel("m");
 		fieldsPanel.add(labelm6, "cell 2 7,alignx left,aligny center");
 		
 		
 		JLabel lblObstacleLength = new JLabel("Obstacle Length");
 		fieldsPanel.add(lblObstacleLength, "cell 0 8,alignx right,aligny center");
 		
 		tfObstacleLength = new JTextField();
 		fieldsPanel.add(tfObstacleLength, "cell 1 8,growx,aligny top");
 		
 
 		JLabel labelm7 = new JLabel("m");
 		fieldsPanel.add(labelm7, "cell 2 8,alignx left,aligny center");
 		
 		
 		JPanel buttonsPanel = new JPanel();
 		contentPane.add(buttonsPanel, "cell 0 1,grow");
 		buttonsPanel.setLayout(new MigLayout("", "[][][grow][][]", "[grow][]"));
 		
 		JButton btnDefault = new JButton("Default");
 		buttonsPanel.add(btnDefault, "cell 0 1");
 		btnDefault.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				physicalRunway.resetAngleOfSlope();
 				physicalRunway.resetBlastAllowance();
 				physicalRunway.resetClearedAndGradedWidth();
 				physicalRunway.resetRESA();
 				physicalRunway.resetRunwayStripWidth();
 				physicalRunway.resetStopway();
 				
 				if(physicalRunway.getObstacle() != null) physicalRunway.getObstacle().resetSize();
 				
 				airportCopy.setModified();
 				if(obstacle != null) obstacle.setModified();
 				
 				notifyAirportObservers();
 				setFormValues();
 			}
 		});
 		
 		
 		JButton btnApply = new JButton("Apply");
 		buttonsPanel.add(btnApply, "cell 3 1");
 		btnApply.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				physicalRunway.setAngleOfSlope(Double.parseDouble(tfAngleOfSlope.getText()));
 				physicalRunway.setBlastAllowance(Double.parseDouble(tfBlastAllowance.getText()));
 				physicalRunway.setClearedAndGradedWidth(Double.parseDouble(tfClearAndGradedWidth.getText()));
 				physicalRunway.setRESA(Double.parseDouble(tfRESA.getText()));
 				physicalRunway.setRunwayStripWidth(Double.parseDouble(tfRunwayStripWidth.getText()));
 				physicalRunway.setStopway(Double.parseDouble(tfStopway.getText()));
 				
 				if(physicalRunway.getObstacle() != null){ 
 					physicalRunway.getObstacle().setWidth(Double.parseDouble(tfObstacleWidth.getText()));
 					physicalRunway.getObstacle().setLength(Double.parseDouble(tfObstacleLength.getText()));
 					physicalRunway.getObstacle().setModified();
 				}
 				
 				airportCopy.setModified();
 	
 				
 				notifyAirportObservers();
 				dispose();
 			}
 		});
 
 		
 		JButton btnClose = new JButton("Close");
 		buttonsPanel.add(btnClose, "cell 4 1");		
 		btnClose.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dispose();
 			}
 		});
 
 		setFormValues();
 		setAlwaysOnTop(true);
 		setVisible(true);
 	}
 	
 	void notifyAirportObservers(){
 		for(AirportObserver ao: airportObservers){
 			ao.updateAirport(airportCopy);
 		}
 	}
 	
 	void setFormValues(){
 		tfRESA.setText(String.valueOf(physicalRunway.getRESA()));
 		tfStopway.setText(String.valueOf(physicalRunway.getStopway()));
 		tfAngleOfSlope.setText(String.valueOf(physicalRunway.getAngleOfSlope()));
 		tfBlastAllowance.setText(String.valueOf(physicalRunway.getBlastAllowance()));
 		tfRunwayStripWidth.setText(String.valueOf(physicalRunway.getRunwayStripWidth()));
 		tfClearAndGradedWidth.setText(String.valueOf(physicalRunway.getClearedAndGradedWidth()));
 		
 		Obstacle obstacle = physicalRunway.getObstacle();
 		
 		if(obstacle != null){
 				tfObstacleWidth.setText(String.valueOf(obstacle.getWidth()));
 				tfObstacleLength.setText(String.valueOf(obstacle.getLength()));
 		}
 		else {
 			tfObstacleWidth.setText("");
 			tfObstacleLength.setText("");
 		}
 			
 	}
 	
 }
