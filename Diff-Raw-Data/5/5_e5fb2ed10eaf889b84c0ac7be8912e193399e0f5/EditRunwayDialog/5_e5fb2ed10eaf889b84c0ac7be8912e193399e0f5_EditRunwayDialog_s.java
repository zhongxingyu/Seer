 package View;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import Model.Airport;
 import Model.AirportObserver;
 import Model.PhysicalRunway;
 import Model.Runway;
 
 import net.miginfocom.swing.MigLayout;
 
 
 
 public class EditRunwayDialog extends JDialog implements AirportObserver{
 
 	private static final long serialVersionUID = 1L;
 	private JPanel contentPane;
 	private JTextField tfLeftASDA;
 	private JTextField tfLeftTORA;
 	private JTextField tfLeftTODA;
 	private JTextField tfLeftLDA;
 	private JTextField tfLeftName;
 	private JTextField tfRightASDA;
 	private JTextField tfRightTORA;
 	private JTextField tfRightTODA;
 	private JTextField tfRightLDA;
 	private JTextField tfRightName;
 	private Airport airport;
 	private JList physicalRunwayJList;
 	private JTextField tfLeftDisplacementThreshold;
 	private JTextField tfRightDisplacedThreshold;
 
 	private List<AirportObserver> airportObservers;
 
 	//I don't really see any benefit to passing this JList in over passing in a physicalRunway...
 
 	public EditRunwayDialog(Airport airport, JList physicalRunwayJList, boolean newRunway, List<AirportObserver> airportObservers) {
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		this.airport = airport;
 		this.physicalRunwayJList = physicalRunwayJList;
 
 		this.airportObservers = airportObservers;
 
 		setResizable(true);
 		setTitle("Edit Runway");
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 490, 315);
 		contentPane = new JPanel();
 
 		setContentPane(contentPane);
 		contentPane.setLayout(new MigLayout("", "[212px][22px][214px]", "[34px][140px][45px]"));
 
 		JPanel leftFeildsPanel = new JPanel();
 		contentPane.add(leftFeildsPanel, "cell 0 1,grow");
 		leftFeildsPanel.setLayout(new MigLayout("", "[37px][37px,grow]", "[][][][][]"));
 
 		JLabel lblLeftTORA = new JLabel("TORA");
 		lblLeftTORA.setToolTipText("Take-Off Run Available ");
 		leftFeildsPanel.add(lblLeftTORA, "cell 0 0,alignx trailing,aligny top");
 
 		tfLeftTORA = new JTextField();
 		lblLeftTORA.setLabelFor(tfLeftTORA);
 		leftFeildsPanel.add(tfLeftTORA, "flowx,cell 1 0,growx");
 		tfLeftTORA.setColumns(10);
 
 		JLabel lblLeftTODA = new JLabel("TODA");
 		lblLeftTODA.setToolTipText("Take-Off Distance Available");
 		leftFeildsPanel.add(lblLeftTODA, "cell 0 1,alignx trailing");
 
 		tfLeftTODA = new JTextField();
 		lblLeftTODA.setLabelFor(tfLeftTODA);
 		leftFeildsPanel.add(tfLeftTODA, "flowx,cell 1 1,growx");
 		tfLeftTODA.setColumns(10);
 
 		JLabel lblLeftASDA = new JLabel("ASDA");
 		lblLeftASDA.setToolTipText("Accelerate Stop Distance Available");
 		leftFeildsPanel.add(lblLeftASDA, "cell 0 2,alignx trailing,aligny top");
 
 		tfLeftASDA = new JTextField();
 		leftFeildsPanel.add(tfLeftASDA, "flowx,cell 1 2,growx");
 		tfLeftASDA.setColumns(10);
 
 		JLabel lbLeftLDA = new JLabel("LDA");
 		lbLeftLDA.setToolTipText("Landing Distance Available");
 		leftFeildsPanel.add(lbLeftLDA, "cell 0 3,alignx trailing");
 
 		tfLeftLDA = new JTextField();
 		lbLeftLDA.setLabelFor(tfLeftLDA);
 		leftFeildsPanel.add(tfLeftLDA, "flowx,cell 1 3,growx");
 		tfLeftLDA.setColumns(10);
 
 		JLabel lblm2 = new JLabel("m");
 		leftFeildsPanel.add(lblm2, "cell 1 0");
 
 		JLabel lblm4 = new JLabel("m");
 		leftFeildsPanel.add(lblm4, "cell 1 1");
 
 		JLabel lblm3 = new JLabel("m");
 		leftFeildsPanel.add(lblm3, "cell 1 3");
 
 		JLabel lbLeftDisplacementThreshold = new JLabel("DT");
 		lbLeftDisplacementThreshold.setToolTipText("Displacement Threshold");
 		leftFeildsPanel.add(lbLeftDisplacementThreshold, "cell 0 4,alignx trailing");
 
 		tfLeftDisplacementThreshold = new JTextField();
 		tfLeftDisplacementThreshold.setColumns(10);
 		leftFeildsPanel.add(tfLeftDisplacementThreshold, "flowx,cell 1 4,growx");
 
 		JLabel lblm5 = new JLabel("m");
 		leftFeildsPanel.add(lblm5, "cell 1 4");
 
 		JLabel lblm1 = new JLabel("m");
 		leftFeildsPanel.add(lblm1, "cell 1 2");
 
 		JPanel leftNamePanel = new JPanel();
 		contentPane.add(leftNamePanel, "cell 0 0,grow");
 		leftNamePanel.setLayout(new MigLayout("", "[68.00,grow][129.00,grow]", "[24px]"));
 
 		JLabel lblLeftRunway = new JLabel("Runway");
 		leftNamePanel.add(lblLeftRunway, "cell 0 0,alignx center,aligny center");
 
 		tfLeftName = new JTextField();	
 		leftNamePanel.add(tfLeftName, "flowx,cell 1 0,growx");
 		tfLeftName.setColumns(10);
 
 		JPanel buttonsPanel = new JPanel();
 		contentPane.add(buttonsPanel, "cell 0 2 3 1,grow");
 
 		JPanel rightFeildsPanel = new JPanel();
 		contentPane.add(rightFeildsPanel, "cell 2 1,grow");
 		rightFeildsPanel.setLayout(new MigLayout("", "[][grow]", "[][][][][]"));
 
 		JLabel lblRightTORA = new JLabel("TORA");
 		lblRightTORA.setToolTipText("Take-Off Run Available ");
 		rightFeildsPanel.add(lblRightTORA, "cell 0 0,alignx trailing");
 
 		tfRightTORA = new JTextField();
 		tfRightTORA.setColumns(10);
 		rightFeildsPanel.add(tfRightTORA, "flowx,cell 1 0,growx");
 
 		JLabel lblRightTODA = new JLabel("TODA");
 		lblRightTODA.setToolTipText("Take-Off Distance Available");
 		rightFeildsPanel.add(lblRightTODA, "cell 0 1,alignx trailing");
 
 		tfRightTODA = new JTextField();
 		tfRightTODA.setColumns(10);
 		rightFeildsPanel.add(tfRightTODA, "flowx,cell 1 1,growx");
 
 		JLabel lblRightASDA = new JLabel("ASDA");
 		lblRightASDA.setToolTipText("Accelerate Stop Distance Available");
 		rightFeildsPanel.add(lblRightASDA, "cell 0 2,alignx trailing");
 
 		tfRightASDA = new JTextField();
 		tfRightASDA.setColumns(10);
 		rightFeildsPanel.add(tfRightASDA, "flowx,cell 1 2,growx");
 
 		JLabel lblRightLDA = new JLabel("LDA");
 		lblRightLDA.setToolTipText("Landing Distance Available");
 		rightFeildsPanel.add(lblRightLDA, "cell 0 3,alignx trailing");
 
 		tfRightLDA = new JTextField();
 		tfRightLDA.setColumns(10);
 		rightFeildsPanel.add(tfRightLDA, "flowx,cell 1 3,growx");
 
 		JLabel labelm7 = new JLabel("m");
 		rightFeildsPanel.add(labelm7, "cell 1 0");
 
 		JLabel labelm8 = new JLabel("m");
 		rightFeildsPanel.add(labelm8, "cell 1 1");
 
 		JLabel labelm9 = new JLabel("m");
 		rightFeildsPanel.add(labelm9, "cell 1 3");
 
 		JLabel lblRightDisplacedThreshold = new JLabel("DT");
 		lblRightDisplacedThreshold.setToolTipText("Displacement Threshold");
 		rightFeildsPanel.add(lblRightDisplacedThreshold, "cell 0 4,alignx trailing");
 
 		tfRightDisplacedThreshold = new JTextField();
 		tfRightDisplacedThreshold.setColumns(10);
 		rightFeildsPanel.add(tfRightDisplacedThreshold, "flowx,cell 1 4,growx");
 
 		JLabel labelm10 = new JLabel("m");
 		rightFeildsPanel.add(labelm10, "cell 1 4");
 
 		JLabel label_m6 = new JLabel("m");
 		rightFeildsPanel.add(label_m6, "cell 1 2");
 
 		JPanel rightNamePanel = new JPanel();
 		contentPane.add(rightNamePanel, "cell 2 0,grow");
 		rightNamePanel.setLayout(new MigLayout("", "[74.00][grow]", "[]"));
 
 		JLabel lblRightRunway = new JLabel("Runway");
 		rightNamePanel.add(lblRightRunway, "cell 0 0,alignx center");
 
 		tfRightName = new JTextField();
 		tfRightName.setColumns(10);
 		rightNamePanel.add(tfRightName, "flowx,cell 1 0,growx");
 
 		JButton btnApply = new JButton("Apply");
 		btnApply.setBounds(266, 11, 80, 23);
 		btnApply.addActionListener(new ApplyListener(newRunway));
 
 		JPanel panel_Spacer2 = new JPanel();
 		rightNamePanel.add(panel_Spacer2, "cell 1 0");
 
 		JPanel panel_Spacer = new JPanel();
 		leftNamePanel.add(panel_Spacer, "cell 1 0");
 		buttonsPanel.setLayout(null);
 		buttonsPanel.add(btnApply);
 
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dispose();
 			}
 		});
 		btnCancel.setBounds(356, 11, 80, 23);
 		buttonsPanel.add(btnCancel);
 
 		if(airport.getPhysicalRunways().size() > 0 & !newRunway){
 			int index = physicalRunwayJList.getSelectedIndex();
 			if(index == -1) index = airport.getPhysicalRunways().indexOf(airport.getCurrentPhysicalRunway());
 
 			Runway leftRunway = airport.getPhysicalRunways().get(index).getRunway(0);
 			Runway rightRunway = airport.getPhysicalRunways().get(index).getRunway(1);
 
 			tfLeftName.setText(leftRunway.getName());
 			tfLeftASDA.setText(Double.toString(leftRunway.getASDA(Runway.DEFAULT)));
 			tfLeftTORA.setText(Double.toString(leftRunway.getTORA(Runway.DEFAULT)));
 			tfLeftTODA.setText(Double.toString(leftRunway.getTODA(Runway.DEFAULT)));
 			tfLeftLDA.setText(Double.toString(leftRunway.getLDA(Runway.DEFAULT)));
 			tfLeftDisplacementThreshold.setText(Double.toString(leftRunway.getDisplacedThreshold(Runway.DEFAULT)));
 
 			tfRightName.setText(rightRunway.getName());
 			tfRightASDA.setText(Double.toString(rightRunway.getASDA(Runway.DEFAULT)));
 			tfRightTORA.setText(Double.toString(rightRunway.getTORA(Runway.DEFAULT)));
 			tfRightTODA.setText(Double.toString(rightRunway.getTODA(Runway.DEFAULT)));
 			tfRightLDA.setText(Double.toString(rightRunway.getLDA(Runway.DEFAULT)));
 			tfRightDisplacedThreshold.setText(Double.toString(rightRunway.getDisplacedThreshold(Runway.DEFAULT)));
 		}		
 
 		setAlwaysOnTop(true);
 		setVisible(true);
 	}
 
 	@Override
 	public void updateAirport(Airport airport) {
 		this.airport = airport;
 
 	}
 
 
 	class ApplyListener implements ActionListener{
 		boolean newRunway;
 
 		public void actionPerformed(ActionEvent e) {
 
 			
 			
 			try{
 			
 			double leftTORA = DoubleParser.parse(tfLeftTORA.getText());
 			double leftTODA = DoubleParser.parse(tfLeftTODA.getText());
 			double leftASDA = DoubleParser.parse(tfLeftASDA.getText());
 			
 			double rightTORA = DoubleParser.parse(tfRightTORA.getText());
 			double rightTODA = DoubleParser.parse(tfRightTODA.getText());
 			double rightASDA = DoubleParser.parse(tfRightASDA.getText());
 			
 			if(tfLeftName.getText().equals("") || tfRightName.getText().equals("")){
 				JOptionPane.showMessageDialog(null, "Both runways must be named", "", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			
 			if(leftTODA < leftTORA || rightTODA < rightTORA){
 				JOptionPane.showMessageDialog(null, "TODA cannot be smaller than TORA", "", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			
			if(leftASDA < leftTORA || rightASDA < rightTORA){
 				JOptionPane.showMessageDialog(null, "ASDA cannot be smaller than TORA", "", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			
			if(leftASDA < leftTODA || rightASDA < rightTODA){
 				JOptionPane.showMessageDialog(null, "ASDA cannot be smaller than TODA", "", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 				
 			if(airport.getPhysicalRunways().size() > 0 & !newRunway){ // get the physical runway and change the values
 				int index = physicalRunwayJList.getSelectedIndex();
 				if(index == -1) index = airport.getPhysicalRunways().indexOf(airport.getCurrentPhysicalRunway());
 				// set the values to what's in the JTextFields
 
 				Runway leftRunway = airport.getPhysicalRunways().get(index).getRunway(0);
 				Runway rightRunway = airport.getPhysicalRunways().get(index).getRunway(1);
 
 				leftRunway.setName(tfLeftName.getText());
 				leftRunway.setASDA(Runway.DEFAULT, DoubleParser.parse(tfLeftASDA.getText()));
 				leftRunway.setTORA(Runway.DEFAULT, DoubleParser.parse(tfLeftTORA.getText()));
 				leftRunway.setTODA(Runway.DEFAULT, DoubleParser.parse(tfLeftTODA.getText()));
 				leftRunway.setLDA(Runway.DEFAULT, DoubleParser.parse(tfLeftLDA.getText()));
 				leftRunway.setDisplacedThreshold(Runway.DEFAULT, DoubleParser.parse(tfLeftDisplacementThreshold.getText()));
 
 				rightRunway.setName(tfRightName.getText());
 				rightRunway.setASDA(Runway.DEFAULT, DoubleParser.parse(tfRightASDA.getText()));
 				rightRunway.setTORA(Runway.DEFAULT, DoubleParser.parse(tfRightTORA.getText()));
 				rightRunway.setTODA(Runway.DEFAULT, DoubleParser.parse(tfRightTODA.getText()));
 				rightRunway.setLDA(Runway.DEFAULT, DoubleParser.parse(tfRightLDA.getText()));
 				rightRunway.setDisplacedThreshold(Runway.DEFAULT, DoubleParser.parse(tfRightDisplacedThreshold.getText()));
 
 				airport.getPhysicalRunways().get(index).calculateParameters();
 
 				airport.getPhysicalRunways().get(index).setId(tfLeftName.getText() + "/" + tfRightName.getText());
 
 			} else { // add a new physical runway and assign the values
 				//TODO: does this bit need a calculate?
 				airport.addPhysicalRunway(
 						new PhysicalRunway(tfLeftName.getText() + "/" +  tfRightName.getText(), 
 								new Runway(tfLeftName.getText(), 
 										DoubleParser.parse(tfLeftTORA.getText()), 
 										DoubleParser.parse(tfLeftASDA.getText()), 
 										DoubleParser.parse(tfLeftTODA.getText()), 
 										DoubleParser.parse(tfLeftLDA.getText()), 
 										DoubleParser.parse(tfLeftDisplacementThreshold.getText())),	
 										new Runway(tfRightName.getText(), 
 												DoubleParser.parse(tfRightTORA.getText()), 
 												DoubleParser.parse(tfRightASDA.getText()), 
 												DoubleParser.parse(tfRightTODA.getText()), 
 												DoubleParser.parse(tfRightLDA.getText()), 
 												DoubleParser.parse(tfRightDisplacedThreshold.getText())))); 
 				if(airport.getPhysicalRunways().size() == 1){
 					airport.setCurrentPhysicalRunway(airport.getPhysicalRunways().get(0));
 					airport.setCurrentRunway(airport.getCurrentPhysicalRunway().getRunway(0));
 				}
 			}
 
 			ArrayList<String> physicalRunwayNames = new ArrayList<String>();
 			for(PhysicalRunway p : airport.getPhysicalRunways()){
 				physicalRunwayNames.add(p.getId());
 			}
 
 			DefaultListModel physicalRunwayListModel = new DefaultListModel();
 			for(int i = 0; i < physicalRunwayNames.size(); i++){
 				physicalRunwayListModel.addElement(physicalRunwayNames.get(i));
 			}
 
 			physicalRunwayJList.setModel(physicalRunwayListModel);
 			physicalRunwayJList.setSelectedIndex(0);
 
 			airport.setModified();
 
 			notifyAirportObservers();
 			dispose();
 			
 			}catch(NumberFormatException nfe){
 				JOptionPane.showMessageDialog(null, "One of the parameters contains an invalid number, please check you inputs and try again.", "", JOptionPane.ERROR_MESSAGE);
 			}
 			
 		}
 
 
 		public ApplyListener(boolean newRunway) {
 			this.newRunway = newRunway;
 		}
 	}
 
 	void notifyAirportObservers(){
 		for(AirportObserver ao: airportObservers){
 			ao.updateAirport(airport);
 		}
 	}
 }
 
 
 class DoubleParser{
 	static double parse(String s) throws NumberFormatException{
 		if(s.equals("")) return 0.0;
 		return Double.parseDouble(s);
 	}
 }
