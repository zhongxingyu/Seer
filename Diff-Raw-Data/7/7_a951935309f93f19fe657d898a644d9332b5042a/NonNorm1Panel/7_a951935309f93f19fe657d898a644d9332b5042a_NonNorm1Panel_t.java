 package gui.panels.subcontrolpanels;
 
 import engine.alex.agent.AlexConveyorAgent;
 import engine.alex.agent.AlexInlineMachineAgent;
 import engine.interfaces.ConveyorFamily;
 import engine.josh.agent.JoshConveyorAgent;
 import engine.josh.agent.JoshInlineMachineAgent;
 import engine.sky.agent.SkyConveyorAgent;
 import engine.sky.agent.SkyMachineAgent;
 import engine.sky.agent.SkyPopUpAgent;
 import gui.panels.ControlPanel;
 import gui.panels.FactoryPanel;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 public class NonNorm1Panel extends JPanel implements ActionListener{
 
 	ControlPanel parent;
 	FactoryPanel fp;
 
 	// Declaration of Swing
 	JComboBox conveyorDropList, popupDropList, offlineDropList, inlineDropList;
 	JButton conveyorJamButton, conveyorUnJamButton;
 	JButton inlineBreakButton, inlineUnBreakButton;
 	JButton popupBreakButton, popupUnBreakButton;
 	JButton offlineBreakButton, offlineUnBreakButton, offlineOnButton, offlineOffButton, offlineRemoveGlassButton, offlineProcessingTimeButton;
 	JButton truckLeaveButton, truckReturnButton;
 	JTextField offlineProcessingTimeField;
 	JLabel offlineProcessingTimeLabel;
 	JLabel conveyorLabel, popupLabel, inlineLabel, offlineLabel, truckLabel;
 
 	// Declaration of Swing Box container
 	JPanel conveyorContainer, popupContainer, inlineContainer, offlineContainer, offlineProcessingTimeContainer, truckContainer;
 
 	// Booleans for the states of agents
 	boolean [] conveyorBreakBool;
 	boolean [] inlineBreakBool;
 	boolean [] popupBreakBool;
 	boolean [] offlineBreakBool;
 	boolean [] offlineOnBool;
 	int [] offlineTimeInt;
 
 	boolean started = false;
 	public NonNorm1Panel(ControlPanel cp)
 	{
 		parent = cp;
 
 		this.setBackground(Color.gray);
 		this.setForeground(Color.black);
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
 		// declaration and initialization of booleans/states
 		conveyorBreakBool = new boolean[15];
 		inlineBreakBool = new boolean[7];
 		popupBreakBool = new boolean[3];
 		offlineBreakBool = new boolean[6];
 		offlineOnBool = new boolean[6];
 		offlineTimeInt = new int[6];
 
 		for(int i = 0; i < 15; i++)
 			conveyorBreakBool[i] = false;
 		for(int i = 0; i < 7; i++)
 			inlineBreakBool[i] = false;
 		for(int i = 0; i < 3; i++)
 			popupBreakBool[i] = false;
 		for(int i = 0; i < 6; i++){
 			offlineBreakBool[i] = false;
 			offlineOnBool[i] = true;
 			offlineTimeInt[i] = 1;
 		}
 
 
 		// initialization of labels
 		conveyorLabel = new JLabel("CONVEYORS");
		//conveyorLabel.setOpaque(true);
		//conveyorLabel.setBackground(Color.red);
 		conveyorLabel.setForeground(Color.blue);
 		conveyorLabel.setPreferredSize(new Dimension(400,15));
 		conveyorLabel.setMaximumSize(new Dimension(400,15));
		//conveyorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		inlineLabel = new JLabel("INLINE MACHINSE");
 		inlineLabel.setForeground(Color.blue);
 		//inlineLabel.setHorizontalAlignment(SwingConstants.LEFT);
 		popupLabel = new JLabel("POP UPS");
 		popupLabel.setForeground(Color.blue);
 		offlineLabel = new JLabel("OFFLINE MACHINES");
 		offlineLabel.setForeground(Color.blue);
 		offlineProcessingTimeLabel = new JLabel("Processing Time");
 		truckLabel = new JLabel("TRUCK");
 		truckLabel.setForeground(Color.blue);
 
 		// initialization of drop downs
 		conveyorDropList = new JComboBox();
 		popupDropList = new JComboBox();
 		offlineDropList = new JComboBox();
 		inlineDropList = new JComboBox();
 
 		// setting size for drop list
 		conveyorDropList.setPreferredSize(new Dimension(200,25));
 		conveyorDropList.setMaximumSize(new Dimension(300,25));
 		popupDropList.setPreferredSize(new Dimension(200,25));
 		popupDropList.setMaximumSize(new Dimension(300,25));
 		inlineDropList.setPreferredSize(new Dimension(200,25));
 		inlineDropList.setMaximumSize(new Dimension(300,25));
 		offlineDropList.setPreferredSize(new Dimension(200,25));
 		offlineDropList.setMaximumSize(new Dimension(200,25));
 
 
 		// Adding the drop down for the Combo Box
 		for(int i = 1; i <= 15; i++){
 			conveyorDropList.addItem("Conveyor " + i);
 		}
 		for(int i = 1; i <= 3; i++){
 			popupDropList.addItem("Popup " + i);
 		}
 
 		// Adding specific drop down for offline
 		offlineDropList.addItem("Driller 1");
 		offlineDropList.addItem("Driller 2");
 		offlineDropList.addItem("Cross-seamer 1");
 		offlineDropList.addItem("Cross-seamer 2");
 		offlineDropList.addItem("Grinder 1");
 		offlineDropList.addItem("Grinder 2");
 
 		// Adding specific drop down for inline
 		inlineDropList.addItem("Cutter");
 		inlineDropList.addItem("Breakout");
 		inlineDropList.addItem("Manual Breakout");
 		inlineDropList.addItem("Washer");
 		inlineDropList.addItem("Painter");
 		inlineDropList.addItem("UV Lamp");
 		inlineDropList.addItem("Oven");
 
 		// Initialization of Buttons
 		conveyorJamButton = new JButton("Jam");
 		conveyorUnJamButton = new JButton("Unjam");
 		inlineBreakButton = new JButton("Break");
 		inlineUnBreakButton = new JButton("Fix");
 		popupBreakButton = new JButton("Break");
 		popupUnBreakButton = new JButton("Fix");
 		offlineBreakButton = new JButton("Break");
 		offlineUnBreakButton = new JButton("Fix");
 		offlineOnButton = new JButton("On");
 		offlineOffButton = new JButton("Off");
 		offlineRemoveGlassButton = new JButton("Remove Glass(Caution!)");
 		offlineProcessingTimeButton = new JButton("Set");
 		truckLeaveButton = new JButton("Leave");
 		truckReturnButton = new JButton("Return");
 
 		// offline Text Field Container
 		offlineProcessingTimeField = new JTextField(4);
 		offlineProcessingTimeField.setPreferredSize(new Dimension(40,25));
 		offlineProcessingTimeField.setMaximumSize(new Dimension(40,25));
 
 		// Set initial states of buttons
 		conveyorJamButton.setEnabled(true);
 		conveyorUnJamButton.setEnabled(false);
 		inlineBreakButton.setEnabled(true);
 		inlineUnBreakButton.setEnabled(false);
 		popupBreakButton.setEnabled(true);
 		popupUnBreakButton.setEnabled(false);
 		truckLeaveButton.setEnabled(true);
 		truckReturnButton.setEnabled(false);
 		offlineOnButton.setEnabled(false);
 		offlineOffButton.setEnabled(true);
 		offlineBreakButton.setEnabled(true);
 		offlineUnBreakButton.setEnabled(false);
 
 		// adding action listeners to buttons
 		conveyorJamButton.addActionListener(this);
 		conveyorUnJamButton.addActionListener(this);
 		inlineBreakButton.addActionListener(this);
 		inlineUnBreakButton.addActionListener(this);
 		popupBreakButton.addActionListener(this);
 		popupUnBreakButton.addActionListener(this);
 		offlineBreakButton.addActionListener(this);
 		offlineUnBreakButton.addActionListener(this);
 		offlineOnButton.addActionListener(this);
 		offlineOffButton.addActionListener(this);
 		offlineProcessingTimeButton.addActionListener(this);
 		offlineRemoveGlassButton.addActionListener(this);
 		offlineDropList.addActionListener(this);
 		inlineDropList.addActionListener(this);
 		popupDropList.addActionListener(this);
 		conveyorDropList.addActionListener(this);
 		truckLeaveButton.addActionListener(this);
 		truckReturnButton.addActionListener(this);
 
 		// Initialization of containers
 		conveyorContainer = new JPanel();
 		inlineContainer = new JPanel();
 		popupContainer = new JPanel();
 		offlineContainer = new JPanel();
 		offlineProcessingTimeContainer = new JPanel();
 		truckContainer = new JPanel();
 		conveyorContainer.setLayout(new BoxLayout(conveyorContainer,BoxLayout.X_AXIS));
 		inlineContainer.setLayout(new BoxLayout(inlineContainer,BoxLayout.X_AXIS));
 		offlineContainer.setLayout(new BoxLayout(offlineContainer,BoxLayout.X_AXIS));
 		popupContainer.setLayout(new BoxLayout(popupContainer,BoxLayout.X_AXIS));
 		offlineProcessingTimeContainer.setLayout(new BoxLayout(offlineProcessingTimeContainer,BoxLayout.X_AXIS));
 		truckContainer.setLayout(new BoxLayout(truckContainer,BoxLayout.X_AXIS));
 
 		// Adding elements to Conveyor Container
 		conveyorContainer.add(conveyorDropList);
 		conveyorContainer.add(conveyorJamButton);
 		conveyorContainer.add(conveyorUnJamButton);
 
 		// Adding elements to inline container
 		inlineContainer.add(inlineDropList);
 		inlineContainer.add(inlineBreakButton);
 		inlineContainer.add(inlineUnBreakButton);
 
 		// Adding elements to popup container
 		popupContainer.add(popupDropList);
 		popupContainer.add(popupBreakButton);
 		popupContainer.add(popupUnBreakButton);
 
 		// Adding elements to offline container
 		offlineContainer.add(offlineDropList);
 		offlineContainer.add(offlineBreakButton);
 		offlineContainer.add(offlineUnBreakButton);
 		offlineContainer.add(offlineOffButton);
 		offlineContainer.add(offlineOnButton);
 
 		// Adding elements to offline Processing Time container
 		offlineProcessingTimeContainer.add(offlineProcessingTimeLabel);
 		offlineProcessingTimeContainer.add(offlineProcessingTimeField);
 		offlineProcessingTimeContainer.add(offlineProcessingTimeButton);
 		offlineProcessingTimeContainer.add(offlineRemoveGlassButton);
 
 		// Adding elements to truck container
 		truckContainer.add(truckLeaveButton);
 		truckContainer.add(truckReturnButton);
 
 		// Adding containers to GUI
 		//this.setAlignmentX(Component.LEFT_ALIGNMENT);
 		this.add(conveyorLabel);
 		this.add(conveyorContainer);
 		this.add(inlineLabel);
 		this.add(inlineContainer);
 		this.add(popupLabel);
 		this.add(popupContainer);
 		this.add(offlineLabel);
 		this.add(offlineContainer);
 		this.add(offlineProcessingTimeContainer);
 		this.add(truckLabel);
 		this.add(truckContainer);
 	}
 
 
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		int index = 0;
 		if(!started){
 			fp = parent.getGuiParent();
 			started = true;
 		}
 		if(ae.getSource() == conveyorDropList){
 			index = conveyorDropList.getSelectedIndex();
 
 			if(conveyorBreakBool[index]){
 				conveyorJamButton.setEnabled(false);
 				conveyorUnJamButton.setEnabled(true);
 			}
 			else{
 				conveyorJamButton.setEnabled(true);
 				conveyorUnJamButton.setEnabled(false);
 			}
 
 		}
 		else if(ae.getSource() == popupDropList){
 			index = popupDropList.getSelectedIndex();
 			if(popupBreakBool[index]){
 				popupBreakButton.setEnabled(false);
 				popupUnBreakButton.setEnabled(true);
 			}
 			else{
 				popupBreakButton.setEnabled(true);
 				popupUnBreakButton.setEnabled(false);
 			}
 		}
 		else if(ae.getSource() == inlineDropList){
 			index = inlineDropList.getSelectedIndex();
 			if(inlineBreakBool[index]){
 				inlineBreakButton.setEnabled(false);
 				inlineUnBreakButton.setEnabled(true);
 			}
 			else{
 				inlineBreakButton.setEnabled(true);
 				inlineUnBreakButton.setEnabled(false);
 			}
 		}
 		else if(ae.getSource() == offlineDropList){
 			index = offlineDropList.getSelectedIndex();
 			if(offlineBreakBool[index]){
 				offlineBreakButton.setEnabled(false);
 				offlineUnBreakButton.setEnabled(true);
 			}
 			else{
 				offlineBreakButton.setEnabled(true);
 				offlineUnBreakButton.setEnabled(false);
 			}
 			if(offlineOnBool[index]){
 				offlineOnButton.setEnabled(false);
 				offlineOffButton.setEnabled(true);
 			}
 			else{
 				offlineOnButton.setEnabled(true);
 				offlineOffButton.setEnabled(false);
 			}
 			String ss = Integer.toString(offlineTimeInt[index]);
 			offlineProcessingTimeField.setText(ss);
 		}
 		else if(ae.getSource() == conveyorJamButton){
 			index = conveyorDropList.getSelectedIndex();
 			conveyorBreakBool[index] = true;
 			conveyorJamButton.setEnabled(false);
 			conveyorUnJamButton.setEnabled(true);
 
 			// Call message
 			if(fp.getConveyorList()[index] instanceof AlexConveyorAgent){
 				((AlexConveyorAgent) fp.getConveyorList()[index]).msgConveyorJammed();
 			}
 			else if(fp.getConveyorList()[index] instanceof JoshConveyorAgent){
 				((JoshConveyorAgent) fp.getConveyorList()[index]).msgConveyorJammed();
 			}
 			else if(fp.getConveyorList()[index] instanceof SkyConveyorAgent){
 				((SkyConveyorAgent) fp.getConveyorList()[index]).msgConveyorJammed();
 			}
 
 
 
 		}
 		else if(ae.getSource() == conveyorUnJamButton){
 			index = conveyorDropList.getSelectedIndex();
 			conveyorBreakBool[index] = false;
 			conveyorJamButton.setEnabled(true);
 			conveyorUnJamButton.setEnabled(false);
 
 			// Call message
 			if(fp.getConveyorList()[index] instanceof AlexConveyorAgent){
 				((AlexConveyorAgent) fp.getConveyorList()[index]).msgConveyorUnjammed();
 			}
 			if(fp.getConveyorList()[index] instanceof JoshConveyorAgent){
 				((JoshConveyorAgent) fp.getConveyorList()[index]).msgConveyorUnjammed();
 			}
 			if(fp.getConveyorList()[index] instanceof SkyConveyorAgent){
 				((SkyConveyorAgent) fp.getConveyorList()[index]).msgConveyorUnjammed();
 			}
 			
 			//fp.getConveyorList()[index].msgConveyorUnjammed();
 		}
 		else if(ae.getSource() == inlineBreakButton){
 			index = inlineDropList.getSelectedIndex();
 			inlineBreakBool[index] = true;
 			inlineBreakButton.setEnabled(false);
 			inlineUnBreakButton.setEnabled(true);
 
 			// Call message
 			if(fp.getInlineList()[index] instanceof AlexInlineMachineAgent){
 				((AlexInlineMachineAgent) fp.getInlineList()[index]).msgInlineMachineBreak();
 			}
 			else if(fp.getInlineList()[index] instanceof JoshInlineMachineAgent){
 				((JoshInlineMachineAgent) fp.getInlineList()[index]).msgInlineMachineBreak();
 			}
 		}
 		else if(ae.getSource() == inlineUnBreakButton){
 			index = inlineDropList.getSelectedIndex();
 			inlineBreakBool[index] = false;
 			inlineBreakButton.setEnabled(true);
 			inlineUnBreakButton.setEnabled(false);
 
 			// Call message
 			if(fp.getInlineList()[index] instanceof AlexInlineMachineAgent){
 				((AlexInlineMachineAgent) fp.getInlineList()[index]).msgInlineMachineUnbreak();
 			}
 			else if(fp.getInlineList()[index] instanceof JoshInlineMachineAgent){
 				((JoshInlineMachineAgent) fp.getInlineList()[index]).msgInlineMachineUnbreak();
 			}
 		}
 		else if(ae.getSource() == popupBreakButton){
 			index = popupDropList.getSelectedIndex();
 			popupBreakBool[index] = true;
 			popupBreakButton.setEnabled(false);
 			popupUnBreakButton.setEnabled(true);
 
 			// Call message
 			((SkyPopUpAgent) fp.getPopUpList()[index]).msgPopUpBreak();
 		}
 		else if(ae.getSource() == popupUnBreakButton){
 			index = popupDropList.getSelectedIndex();
 			popupBreakBool[index] = false;
 			popupBreakButton.setEnabled(true);
 			popupUnBreakButton.setEnabled(false);
 
 			// Call message
 			((SkyPopUpAgent) fp.getPopUpList()[index]).msgPopUpUnbreak();
 		}
 		else if(ae.getSource() == offlineBreakButton){
 			index = offlineDropList.getSelectedIndex();
 			offlineBreakBool[index] = true;
 			offlineBreakButton.setEnabled(false);
 			offlineUnBreakButton.setEnabled(true);
 
 			// Call message
 			((SkyMachineAgent) fp.getOfflineList()[index]).msgOfflineMachineBreak();
 		}
 		else if(ae.getSource() == offlineUnBreakButton){
 			index = offlineDropList.getSelectedIndex();
 			offlineBreakBool[index] = false;
 			offlineBreakButton.setEnabled(true);
 			offlineUnBreakButton.setEnabled(false);
 
 			// Call message
 			((SkyMachineAgent) fp.getOfflineList()[index]).msgOfflineMachineUnbreak();
 			//fp.getOfflineList()[index].msgOfflineMachineUnbreak();
 		}
 		else if(ae.getSource() == offlineOnButton){
 			index = offlineDropList.getSelectedIndex();
 			offlineOnBool[index] = true;
 			offlineOnButton.setEnabled(false);
 			offlineOffButton.setEnabled(true);
 
 			// Call message
 
 			((SkyPopUpAgent) fp.getPopUpList()[index/2]).msgOfflineMachineOn(index);
 
 		}
 		else if(ae.getSource() == offlineOffButton){
 			index = offlineDropList.getSelectedIndex();
 			offlineOnBool[index] = false;
 			offlineOnButton.setEnabled(true);
 			offlineOffButton.setEnabled(false);
 
 			// Call message
 			((SkyPopUpAgent) fp.getPopUpList()[index/2]).msgOfflineMachineOff(index);
 		}
 		else if(ae.getSource() == offlineProcessingTimeButton){
 			index = offlineDropList.getSelectedIndex();
 			String ss = offlineProcessingTimeField.getText();
 			Integer pTime = 0;
 			try{
 				pTime = Integer.parseInt(ss);
 			}
 			catch(Exception e){
 				System.out.println("Enter an integer for Processing Time.");
 				return;
 			}
 			offlineTimeInt[index] = pTime;
 			// Call message
 			((SkyMachineAgent) fp.getOfflineList()[index]).msgChangeProcessingTime((int) pTime);
 		}
 		else if(ae.getSource() == offlineRemoveGlassButton){
 			index = offlineDropList.getSelectedIndex();
 			System.out.println(index);
 			// Call message
 			((SkyMachineAgent) fp.getOfflineList()[index]).msgRemoveGlass();
 		}
 		else if(ae.getSource() == truckLeaveButton){
 			truckLeaveButton.setEnabled(false);
 			truckReturnButton.setEnabled(true);
 			// Call message
 			fp.getTruck().msgTruckLeave();
 		}else if(ae.getSource() == truckReturnButton){
 			truckLeaveButton.setEnabled(true);
 			truckReturnButton.setEnabled(false);
 
 			// Call message
 			fp.getTruck().msgTruckReturn();
 		}
 
 	}
 
 	public ControlPanel getGuiParent()
 	{
 		return parent;
 	}
 
 }
